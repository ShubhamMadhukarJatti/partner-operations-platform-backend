package com.sharkdom.partnerattribution.dto;

import com.sharkdom.partnerattribution.enums.CoSellMotion;
import com.sharkdom.partnerattribution.enums.OverlapType;
import com.sharkdom.partnerattribution.enums.PartnerAttributionAction;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SharedAccountResponse {

    private String accountName;
    private String website;

    private OverlapType overlapType;

    private Integer opportunityScore;

    private String yourStage;
    private String partnerStage;

    private String estimatedAcv;

    private PartnerAttributionAction action;
    private CoSellMotion motion;

    private String dealId;

    private String ownerId;

    private String partnerDealId;

    private String partnerDealOwnerId;
}
