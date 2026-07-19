package com.sharkdom.entity.referral;

import com.sharkdom.constants.campaign.CampaignStatus;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.model.referral.CommissionType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.*;

@Entity
@Table(name = "campaign")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignEntity extends BaseEntity {
    private Long organizationId;
    private String referralCode;
    private String urlRef;
    private String emailRef;
    private CampaignStatus status;
    private String referralLink;
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
    @Transient
    private Integer impressionCount;
    @Transient
    private Integer leadsCount;
    private String description;
}
