package com.sharkdom.entity.referral;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "referral")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralEntity extends BaseEntity {
    @Column(unique = true)
    private String referralCode;
    private Long organizationId;
    private String testWebhookUrl;
    private String prodWebhookUrl;
}
