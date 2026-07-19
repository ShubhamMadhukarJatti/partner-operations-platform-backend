package com.sharkdom.entity.referral;

import com.sharkdom.entity.BaseEntity;
import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "referral_whitelist")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralWhitelist extends BaseEntity {
    @Column(unique = true)
    String domain;
}
