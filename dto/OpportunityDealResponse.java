package com.sharkdom.partnerattribution.dto;

import com.sharkdom.partnerattribution.enums.DealActionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpportunityDealResponse {

    private String dealName;
    private String dealStage;
    private String associatedCompanyId;
    private String dealOwner;
    private String amountAcv;
    private String closeDate;
    private String dealId;
    private String targetPartnerDealId;
    private String ownerId;
    private String targetOwnerId;
    private String pipeline;
    private String lastActivityDate;
    private String dealType;
    private String associatedContactId;
    private String website;
    private String companySize;
    private String industry;
    private String country;
    private DealActionType dealActionType;
}
