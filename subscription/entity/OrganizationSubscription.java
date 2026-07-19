package com.sharkdom.subscription.entity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.reseller.entity.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "t_organization_subscription")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrganizationSubscription extends BaseEntity {

    private Long organizationId;

    private String stripeSubscriptionId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime gracePeriodEnd;

    private LocalDateTime downgradeEffectiveDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "t_org_active_suites")
    @Column(name = "suite_key")
    @Enumerated(EnumType.STRING)
    private List<SuiteKey> activeSuites;
}