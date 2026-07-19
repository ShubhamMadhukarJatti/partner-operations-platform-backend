package com.sharkdom.entity.payment;

import com.sharkdom.constants.PlanType;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "razorpay_subscription")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionEntity extends BaseEntity {
    private String subscriptionId;
    private Long organizationId;
    private PlanType planType;
}
