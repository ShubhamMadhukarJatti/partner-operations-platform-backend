package com.sharkdom.model.referral;

import com.sharkdom.constants.campaign.CampaignStatus;
import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ReferralLinkResponse {
    private Long id;
    private String referralLink;
    private String referralCode;
    private Long organizationId;
    private String urlRef;
    private String emailRef;
    private CampaignStatus status;
    private String partnerOrganizationName;
    private String domain;
    private Long partnerId;
    boolean emailVerified = false;
    boolean domainVerified = false;
    private String programName;
    private boolean commission;
    private Integer commissionPercentage;
    private Integer minimumThreshold;
    private CommissionType commissionType;
    private Integer impressionCount;
    private Integer leadsCount;
    private String description;
    private String testWebhookUrl;
    private String prodWebhookUrl;
}
