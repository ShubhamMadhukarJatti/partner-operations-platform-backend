package com.sharkdom.entity.campaign;

import com.sharkdom.constants.campaign.CampaignType;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "campaign_integration")
public class CampaignIntegrationEntity extends BaseEntity {
    private Long organizationId;
    @Column(columnDefinition = "LONGTEXT")
    private String campaignValue;
    private CampaignType campaignType;
}
