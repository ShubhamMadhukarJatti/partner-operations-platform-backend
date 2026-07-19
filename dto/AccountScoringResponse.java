package com.sharkdom.partnerattribution.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountScoringResponse {

    private String accountName;

    private int accountFitScore;
    private int partnerRelationshipStrength;
    private int timingSignalScore;
    private int salesReadinessScore;

    private int overlapScore;

    private BadgeType badgeType;
}