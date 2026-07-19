package com.sharkdom.heathscore;

import com.sharkdom.constants.partnerDeals.DealStage;
import lombok.Data;

@Data
public class CoSellHealthRequest {

    // Deal Info
    private DealStage dealStage;

    private Double dealSize;

    private Boolean icpMatched;

    private Integer stakeholderContacts;

    private Boolean championEngaged;

    private Boolean competitiveThreat;

    private Boolean budgetConfirmed;

    // Partner Metrics
    private Double partnerCloseRateWithCompany;

    private Double partnerGeneralCloseRate;

    private Integer contactCount;

    private Boolean signedAgreement;

    // Responsiveness
    private Integer lastUpdateDaysAgo;

    private Double avgResponseHours;

    private Integer activitiesLast7Days;

    private Boolean commissionAllocated;

    private Boolean mdfAllocated;

    // Alignment
    private DealStage partnerStage;

    private Boolean committeeFormed;

    private Integer projectedCloseDateDifferenceDays;

    // Timeline
    private Integer daysToClose;

    private StakeholderVelocity stakeholderVelocity;

    private Boolean currentBudgetCycle;

    private Momentum momentum;

    private ProcurementRisk procurementRisk;

    private Boolean competitorMentioned;

    private Boolean legalReviewRequired;

    private Boolean accelerating;

    private Boolean contractFirmCloseDate;
}