package com.sharkdom.partnerattribution.dto;

import lombok.Data;

@Data
public class SharedAccountDTO {

    private String name;
    private String domain;

    private String overlapType;
    private Integer opportunityScore;

    private String yourStage;
    private String partnerStage;

    private Integer estimatedACV;

    private String recommendedAction;

    private String targetPartnerDealId;

    private String currentPartnerDealId;

    private String currentPartnerDealOwnerId;

    private String targetPartnerDealOwnerId;

}