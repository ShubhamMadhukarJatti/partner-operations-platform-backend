package com.sharkdom.partnerattribution.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PartnerPriorityService {

    /**
     * Converts overlap score into sales-friendly priority buckets.
     */
    public PriorityLevel calculatePriorityLevel(double overlapScore) {

        log.info("""
                Starting priority level calculation.
                overlapScore={}
                """, overlapScore);

        /*
         * HIGH PRIORITY
         */
        if (overlapScore >= 80) {

            log.info("""
                    Priority level determined as HIGH.
                    overlapScore={}
                    """, overlapScore);

            return PriorityLevel.HIGH;
        }

        /*
         * MEDIUM PRIORITY
         */
        if (overlapScore >= 60) {

            log.info("""
                    Priority level determined as MEDIUM.
                    overlapScore={}
                    """, overlapScore);

            return PriorityLevel.MEDIUM;
        }

        /*
         * LOW PRIORITY
         */
        if (overlapScore >= 40) {

            log.info("""
                    Priority level determined as LOW.
                    overlapScore={}
                    """, overlapScore);

            return PriorityLevel.LOW;
        }

        /*
         * WATCH / UNKNOWN
         */
        log.info("""
                Priority level determined as WATCH.
                overlapScore={}
                """, overlapScore);

        return PriorityLevel.WATCH;
    }
}
