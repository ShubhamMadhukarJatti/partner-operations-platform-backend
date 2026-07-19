package com.sharkdom.heathscore;

import com.sharkdom.constants.partnerDeals.DealStage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CoSellHealthScoreService {

    private ScoreResult calculateWinProbabilityWithCoSell(CoSellHealthRequest request) {

        double baseWinProb = getBaseWinProbability(request.getDealStage());

        double partnerInfluence =
                (request.getPartnerCloseRateWithCompany() * 0.4)
                        + (request.getPartnerGeneralCloseRate() * 0.3)
                        + ((request.getContactCount() / 10.0) * 0.2)
                        + (Boolean.TRUE.equals(request.getSignedAgreement()) ? 0.1 : 0);

        double coSellWinProb =
                baseWinProb + (baseWinProb * partnerInfluence);

        int points;

        if (coSellWinProb >= 75) {
            points = 35;
        } else if (coSellWinProb >= 60) {
            points = 28;
        } else if (coSellWinProb >= 50) {
            points = 21;
        } else if (coSellWinProb >= 40) {
            points = 14;
        } else {
            points = 0;
        }

        return new ScoreResult(coSellWinProb, points);
    }

    private ScoreResult calculateDirectWinProbability(CoSellHealthRequest request) {

        double probability = getBaseWinProbability(request.getDealStage());

        if (Boolean.TRUE.equals(request.getIcpMatched())) {
            probability += 5;
        }

        if (request.getStakeholderContacts() >= 3) {
            probability += 5;
        }

        if (Boolean.TRUE.equals(request.getChampionEngaged())) {
            probability += 3;
        }

        if (Boolean.TRUE.equals(request.getCompetitiveThreat())) {
            probability -= 5;
        }

        if (!Boolean.TRUE.equals(request.getBudgetConfirmed())) {
            probability -= 3;
        }

        int points;

        if (probability >= 60) {
            points = 10;
        } else if (probability >= 40) {
            points = 6;
        } else {
            points = 2;
        }

        return new ScoreResult(probability, points);
    }

    private int calculatePartnerResponsiveness(CoSellHealthRequest request) {

        int total = 0;

        // Recency
        if (request.getLastUpdateDaysAgo() < 2) {
            total += 8;
        } else if (request.getLastUpdateDaysAgo() <= 7) {
            total += 5;
        } else if (request.getLastUpdateDaysAgo() <= 14) {
            total += 2;
        }

        // Response time
        if (request.getAvgResponseHours() < 4) {
            total += 6;
        } else if (request.getAvgResponseHours() <= 24) {
            total += 4;
        } else {
            total += 2;
        }

        // Activity
        if (request.getActivitiesLast7Days() >= 3) {
            total += 4;
        } else if (request.getActivitiesLast7Days() >= 1) {
            total += 2;
        }

        // Skin in game
        if (Boolean.TRUE.equals(request.getCommissionAllocated())) {
            total += 2;
        }

        if (Boolean.TRUE.equals(request.getMdfAllocated())) {
            total += 2;
        }

        if (total >= 18) {
            return 20;
        } else if (total >= 14) {
            return 16;
        } else if (total >= 10) {
            return 10;
        }

        return 4;
    }

    private int calculateStageAlignment(CoSellHealthRequest request) {

        int total = 0;

        DealStage seller = request.getDealStage();
        DealStage partner = request.getPartnerStage();

        if ((seller == DealStage.PROPOSAL || seller == DealStage.NEGOTIATION)
                && (partner == DealStage.PROPOSAL || partner == DealStage.NEGOTIATION)) {

            total += 8;

        } else if ((seller == DealStage.DEMO && partner == DealStage.PROPOSAL)
                || (seller == DealStage.PROPOSAL && partner == DealStage.DEMO)) {

            total += 5;

        } else if ((seller == DealStage.DISCOVERY && partner == DealStage.DEMO)
                || (seller == DealStage.DEMO && partner == DealStage.DISCOVERY)) {

            total += 2;
        }

        // Budget readiness
        if (Boolean.TRUE.equals(request.getBudgetConfirmed())
                && Boolean.TRUE.equals(request.getCommitteeFormed())) {

            total += 5;

        } else if (Boolean.TRUE.equals(request.getBudgetConfirmed())) {

            total += 3;
        }

        // Close date alignment
        if (request.getProjectedCloseDateDifferenceDays() <= 7) {
            total += 2;
        }

        if (total >= 13) {
            return 15;
        } else if (total >= 9) {
            return 12;
        } else if (total >= 5) {
            return 7;
        }

        return 2;
    }

    private int calculateTimelineRisk(CoSellHealthRequest request) {

        int total = 0;

        // Days to close
        if (request.getDaysToClose() > 60) {
            total += 2;
        } else if (request.getDaysToClose() >= 30) {
            total += 5;
        } else if (request.getDaysToClose() >= 14) {
            total += 8;
        } else {
            total += 10;
        }

        // Velocity
        switch (request.getStakeholderVelocity()) {
            case FAST -> total += 3;
            case NORMAL -> total += 2;
            case SLOW -> total += 0;
        }

        // Budget cycle
        total += Boolean.TRUE.equals(request.getCurrentBudgetCycle()) ? 2 : -2;

        // Competitor
        if (Boolean.TRUE.equals(request.getCompetitorMentioned())) {
            total += 2;
        }

        // Momentum
        switch (request.getMomentum()) {
            case ACCELERATING -> total += 2;
            case STEADY -> total += 1;
            case STALLING -> total -= 1;
        }

        // Procurement
        switch (request.getProcurementRisk()) {
            case NOT_REQUIRED -> total += 2;
            case STARTED -> total += 1;
            case NOT_STARTED -> total += 0;
        }

        if (total >= 20) {
            return 2;
        } else if (total >= 14) {
            return 5;
        }

        return 10;
    }

    private LocalDate calculateProjectedCloseDate(CoSellHealthRequest request) {

        int days = switch (request.getDealStage()) {

            case DISCOVERY -> 45;
            case DEMO -> 35;
            case APPROVED -> 0;
            case WAITING_FOR_APPROVAL -> 0;
            case REJECTED -> 0;
            case CLOSED -> 0;
            case EXPIRED -> 0;
            case APPOINTMENT_SCHEDULED -> 0;
            case QUALIFIED_TO_BUY -> 0;
            case PRESENTATION_SCHEDULED -> 0;
            case DECISION_MAKER_BOUGHT_IN -> 0;
            case CONTRACT_SENT -> 0;
            case QUALIFICATION -> 0;
            case NEEDS_ANALYSIS -> 0;
            case PROPOSAL -> 21;
            case NEGOTIATION -> 14;
            case CLOSED_WON -> 0;
            case CLOSED_LOST -> 0;
            case VALUE_PROPOSITION -> 0;
            case IDENTIFY_DECISION_MAKERS -> 0;
            case PROPOSAL_PRICE_QUOTE -> 0;
            case NEGOTIATION_REVIEW -> 0;
        };

        if (!Boolean.TRUE.equals(request.getBudgetConfirmed())) {
            days += 7;
        }

        if (Boolean.TRUE.equals(request.getLegalReviewRequired())) {
            days += 7;
        }

        if (Boolean.TRUE.equals(request.getAccelerating())) {
            days -= 7;
        }

        if (Boolean.TRUE.equals(request.getContractFirmCloseDate())) {
            days -= 14;
        }

        return LocalDate.now().plusDays(days);
    }

    public CoSellHealthResponse calculateHealthScore(
            CoSellHealthRequest request
    ) {

        ScoreResult coSell =
                calculateWinProbabilityWithCoSell(request);

        ScoreResult direct =
                calculateDirectWinProbability(request);

        int responsiveness =
                calculatePartnerResponsiveness(request);

        int alignment =
                calculateStageAlignment(request);

        int timeline =
                calculateTimelineRisk(request);

        LocalDate projectedCloseDate =
                calculateProjectedCloseDate(request);

        int total =
                coSell.getPoints()
                        + responsiveness
                        + alignment
                        + timeline
                        + (int) (direct.getPoints() * 0.15);

        String status = getHealthStatus(total);

        return CoSellHealthResponse.builder()
                .coSellWinProbability(coSell.getProbability())
                .coSellWinPoints(coSell.getPoints())
                .directWinProbability(direct.getProbability())
                .directWinPoints(direct.getPoints())
                .partnerResponsivenessPoints(responsiveness)
                .stageAlignmentPoints(alignment)
                .timelineRiskPoints(timeline)
                .projectedCloseDate(projectedCloseDate)
                .finalScore(total)
                .healthStatus(status)
                .build();
    }

    private String getHealthStatus(int score) {

        if (score >= 80) {
            return "EXCELLENT";
        } else if (score >= 65) {
            return "GOOD";
        } else if (score >= 50) {
            return "FAIR";
        } else if (score >= 35) {
            return "AT_RISK";
        }

        return "CRITICAL";
    }

    private double getBaseWinProbability(DealStage dealStage) {

        if (dealStage == null) {

            return 20.0;
        }

        return switch (dealStage) {

            case DISCOVERY -> 20.0;

            case DEMO -> 35.0;

            case PROPOSAL -> 52.0;

            case NEGOTIATION -> 68.0;

            case CLOSED_WON -> 100.0;

            case APPROVED -> 0;
            case WAITING_FOR_APPROVAL -> 0;
            case REJECTED -> 0;
            case CLOSED -> 0;
            case EXPIRED -> 0;
            case APPOINTMENT_SCHEDULED -> 0;
            case QUALIFIED_TO_BUY -> 0;
            case PRESENTATION_SCHEDULED -> 0;
            case DECISION_MAKER_BOUGHT_IN -> 0;
            case CONTRACT_SENT -> 0;
            case QUALIFICATION -> 0;
            case NEEDS_ANALYSIS -> 0;
            case CLOSED_LOST -> 0;
            case VALUE_PROPOSITION -> 0;
            case IDENTIFY_DECISION_MAKERS -> 0;
            case PROPOSAL_PRICE_QUOTE -> 0;
            case NEGOTIATION_REVIEW -> 0;
        };
    }


}