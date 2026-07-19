package com.sharkdom.partnerattribution.service;

import com.sharkdom.partnerattribution.dto.AccountScoringRequest;
import com.sharkdom.partnerattribution.dto.AccountScoringResponse;
import com.sharkdom.partnerattribution.dto.BadgeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OverlapScoringService {

    public AccountScoringResponse calculateOverlapScore(AccountScoringRequest request) {

        log.info("Starting overlap score calculation for account: {}", request.getAccountName());

        int accountFitScore = calculateAccountFitScore(request);
        int partnerRelationshipStrength = calculatePartnerRelationshipStrength(request);
        int timingSignalScore = calculateTimingSignalScore(request);
        int salesReadinessScore = calculateSalesReadinessScore(request);

        log.info("Calculated component scores for account: {}", request.getAccountName());
        log.info("Account Fit Score: {}", accountFitScore);
        log.info("Partner Relationship Strength: {}", partnerRelationshipStrength);
        log.info("Timing Signal Score: {}", timingSignalScore);
        log.info("Sales Readiness Score: {}", salesReadinessScore);

        double overlapScore =
                (accountFitScore * 0.40) +
                        (partnerRelationshipStrength * 0.25) +
                        (timingSignalScore * 0.20) +
                        (salesReadinessScore * 0.15);

        int finalScore = (int) Math.round(overlapScore);

        BadgeType badgeType = determineBadge(finalScore);

        log.info("Final overlap score calculated for account: {}", request.getAccountName());
        log.info("Final Score: {}", finalScore);
        log.info("Assigned Badge: {}", badgeType);

        return AccountScoringResponse.builder()
                .accountName(request.getAccountName())
                .accountFitScore(accountFitScore)
                .partnerRelationshipStrength(partnerRelationshipStrength)
                .timingSignalScore(timingSignalScore)
                .salesReadinessScore(salesReadinessScore)
                .overlapScore(finalScore)
                .badgeType(badgeType)
                .build();
    }

    private int calculateAccountFitScore(AccountScoringRequest request) {

        log.debug("Calculating account fit score");

        int score = 0;

        if (request.isTopIndustryMatch()) {
            score += 30;
            log.debug("Industry matched top verticals. Added 30 points");
        }

        if (request.isIcpCompanySizeMatch()) {
            score += 20;
            log.debug("Company size matched ICP. Added 20 points");
        }

        if (request.isActiveMarketMatch()) {
            score += 20;
            log.debug("Geography matched active markets. Added 20 points");
        }

        if (request.isComplementaryTechStack()) {
            score += 15;
            log.debug("Complementary tech stack found. Added 15 points");
        }

        if (request.isTargetAccountFlagged()) {
            score += 15;
            log.debug("Target account manually flagged. Added 15 points");
        }

        score = Math.min(score, 100);

        log.debug("Final account fit score: {}", score);

        return score;
    }

    private int calculatePartnerRelationshipStrength(AccountScoringRequest request) {

        log.debug("Calculating partner relationship strength");

        String partnerStatus = request.getPartnerRelationshipStatus();

        int score;

        switch (partnerStatus) {

            case "ACTIVE_CUSTOMER":
                score = 100;
                break;

            case "CLOSED_WON_RECENT":
                score = 80;
                break;

            case "ACTIVE_OPPORTUNITY":
                score = 60;
                break;

            case "PAST_ENGAGEMENT":
                score = 40;
                break;

            case "CONTACT_ONLY":
                score = 20;
                break;

            default:
                score = 0;
        }

        log.debug("Partner relationship strength score: {}", score);

        return score;
    }

    private int calculateTimingSignalScore(AccountScoringRequest request) {

        log.debug("Calculating timing signal score");

        int score = 0;

        if (request.isRecentFunding()) {
            score += 25;
            log.debug("Recent funding detected. Added 25 points");
        }

        if (request.isNewExecutiveJoined()) {
            score += 25;
            log.debug("New executive joined. Added 25 points");
        }

        if (request.isRecentWebsiteVisit()) {
            score += 20;
            log.debug("Recent website visit detected. Added 20 points");
        }

        if (request.isExpansionNewsDetected()) {
            score += 20;
            log.debug("Expansion or acquisition news detected. Added 20 points");
        }

        if (request.isRenewalWindowApproaching()) {
            score += 10;
            log.debug("Renewal window approaching. Added 10 points");
        }

        score = Math.min(score, 100);

        log.debug("Final timing signal score: {}", score);

        return score;
    }

    private int calculateSalesReadinessScore(AccountScoringRequest request) {

        log.debug("Calculating sales readiness score");

        String crmStage = request.getCrmStage();

        int score;

        switch (crmStage) {

            case "DEMO_SCHEDULED":
            case "PROPOSAL":
            case "NEGOTIATION":
            case "CLOSED_WON":
                score = 100;
                break;

            case "DISCOVERY":
            case "MQL":
                score = 80;
                break;

            case "LEAD":
                score = 60;
                break;

            case "ICP_MATCH_ONLY":
                score = 40;
                break;

            case "NO_SIGNAL":
                score = 20;
                break;

            case "DISQUALIFIED":
                score = 0;
                break;

            default:
                score = 0;
        }

        log.debug("Sales readiness score: {}", score);

        return score;
    }

    private BadgeType determineBadge(int score) {

        log.debug("Determining badge type for score: {}", score);

        if (score >= 80) {
            return BadgeType.CO_SELL_READY;
        }

        if (score >= 60) {
            return BadgeType.IN_PIPELINE;
        }

        if (score >= 40) {
            return BadgeType.MONITOR;
        }

        return BadgeType.LOW_PRIORITY;
    }
}