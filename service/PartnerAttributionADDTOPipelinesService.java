package com.sharkdom.partnerattribution.service;

import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.entity.ai.OverlapRecordFieldEntity;
import com.sharkdom.entity.partenearDeals.Deal;
import com.sharkdom.partnerattribution.dto.AttributionResult;
import com.sharkdom.partnerattribution.enums.AttributionTier;
import com.sharkdom.partnerattribution.enums.OverlapSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerAttributionADDTOPipelinesService {

    /**
     * Main method to calculate attribution tier and strength.
     */
    public AttributionResult calculateAttribution(OverlapRecordFieldEntity deal,
                                                  boolean formalWarmIntro,
                                                  boolean overlapVisibleDuringCycle,
                                                  OverlapSignal overlapSignal) {

        log.info("Starting attribution calculation for dealId={}", deal.getId());

        /*
         * Highest Priority:
         * SOURCED Attribution
         */
        if (formalWarmIntro) {

            log.info("""
                    Deal {} marked as SOURCED.
                    Reason: Formal warm intro exists.
                    """, deal.getId());

            return AttributionResult.builder()
                    .tier(AttributionTier.SOURCED)
                    .baseTierValue(1.0)
                    .decayFactor(1.0)
                    .attributionStrength(1.0)
                    .reason("Formal warm intro by partner")
                    .build();
        }

        /*
         * INFLUENCED Attribution
         */
        if (overlapSignal != null && overlapSignal.isActive()) {

            long overlapDays = calculateOverlapDays(
                    overlapSignal.getDetectedAt(),
                    LocalDateTime.parse(deal.getCreationDate())
            );


            double decayFactor = calculateDecayFactor(overlapDays);

            /*
             * Attribution valid only if within 180 days
             */
            if (overlapDays <= 180) {

                double baseTierValue = 0.7;
                double attributionStrength = baseTierValue * decayFactor;

                log.info("""
                        Deal {} marked as INFLUENCED.
                        baseTierValue={}
                        decayFactor={}
                        attributionStrength={}
                        """,
                        deal.getId(),
                        baseTierValue,
                        decayFactor,
                        attributionStrength
                );

                return AttributionResult.builder()
                        .tier(AttributionTier.INFLUENCED)
                        .baseTierValue(baseTierValue)
                        .decayFactor(decayFactor)
                        .attributionStrength(attributionStrength)
                        .reason("Overlap signal active before deal creation")
                        .build();
            }

            log.warn("""
                    Deal {} overlap signal expired.
                    overlapDays={} exceeded max threshold.
                    """,
                    deal.getId(),
                    overlapDays
            );
        }

        /*
         * VISIBILITY Attribution
         */
        if (overlapVisibleDuringCycle) {

            double baseTierValue = 0.3;
            double decayFactor = 0.5;
            double attributionStrength = baseTierValue * decayFactor;

            log.info("""
                    Deal {} marked as VISIBILITY.
                    Attribution strength={}
                    """,
                    deal.getId(),
                    attributionStrength
            );

            return AttributionResult.builder()
                    .tier(AttributionTier.VISIBILITY)
                    .baseTierValue(baseTierValue)
                    .decayFactor(decayFactor)
                    .attributionStrength(attributionStrength)
                    .reason("Partner overlap visible during deal cycle")
                    .build();
        }

        /*
         * No Attribution
         */
        log.info("""
                No attribution assigned for dealId={}
                """, deal.getId());

        return AttributionResult.builder()
                .tier(null)
                .baseTierValue(0.0)
                .decayFactor(0.0)
                .attributionStrength(0.0)
                .reason("No partner attribution")
                .build();
    }

    /**
     * Calculates overlap days between signal detection and deal creation.
     */
    private long calculateOverlapDays(LocalDateTime overlapDetectedAt,
                                      LocalDateTime dealCreatedAt) {

        long days = Duration.between(
                overlapDetectedAt,
                dealCreatedAt
        ).toDays();

        log.debug("""
                Calculated overlap days={}
                overlapDetectedAt={}
                dealCreatedAt={}
                """,
                days,
                overlapDetectedAt,
                dealCreatedAt
        );

        return Math.max(days, 0);
    }

    /**
     * Decay multiplier logic.
     */
    private double calculateDecayFactor(long overlapDays) {

        double decayFactor;

        if (overlapDays < 30) {
            decayFactor = 1.0;
        } else if (overlapDays < 60) {
            decayFactor = 0.8;
        } else if (overlapDays < 90) {
            decayFactor = 0.6;
        } else if (overlapDays < 180) {
            decayFactor = 0.4;
        } else {
            decayFactor = 0.2;
        }

        log.debug("""
                Decay factor calculated.
                overlapDays={}
                decayFactor={}
                """,
                overlapDays,
                decayFactor
        );

        return decayFactor;
    }
}