package com.sharkdom.entity.referral;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "invite_campaign")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteCampaignEntity extends BaseEntity {
    private Long partnerId;
    private Long campaignId;
}
