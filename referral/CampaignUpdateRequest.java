package com.sharkdom.model.referral;

import com.sharkdom.constants.campaign.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder

public class CampaignUpdateRequest {
    private Long organizationId;
    private String referralCode;
    private String urlRef;
    private String emailRef;
    private String referralLink;
    private String partnerOrganizationName;
    private Long partnerId;
    private CampaignStatus status;
    private String programName;
    private boolean commission;
    private Integer commissionPercentage;
    private Integer minimumThreshold;
    private CommissionType commissionType;
    private String description;
}
