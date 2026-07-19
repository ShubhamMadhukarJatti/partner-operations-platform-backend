package com.sharkdom.entity.referral;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "campaign_stats")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignStatsEntity extends BaseEntity {
    private Long organizationId;
    private Long leadsCount;
    private Long partnerCount;
}
