package com.sharkdom.entity.referral;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "impressions")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpressionEntity extends BaseEntity {
    private String ipAddress;
    private String referralCode;
}

