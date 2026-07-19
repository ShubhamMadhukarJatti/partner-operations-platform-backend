package com.sharkdom.partnerattribution.dto;

import lombok.Data;

@Data
public class AccountScoringRequest {

    private String accountName;

    // Account Fit Signals
    private boolean topIndustryMatch;
    private boolean icpCompanySizeMatch;
    private boolean activeMarketMatch;
    private boolean complementaryTechStack;
    private boolean targetAccountFlagged;

    // Partner Relationship
    private String partnerRelationshipStatus;

    // Timing Signals
    private boolean recentFunding;
    private boolean newExecutiveJoined;
    private boolean recentWebsiteVisit;
    private boolean expansionNewsDetected;
    private boolean renewalWindowApproaching;

    // CRM Readiness
    private String crmStage;
}