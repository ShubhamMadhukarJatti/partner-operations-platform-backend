package com.sharkdom.subscription.repository;

import com.sharkdom.reseller.entity.PaymentStatus;
import com.sharkdom.subscription.entity.OrganizationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationSubscriptionRepository
        extends JpaRepository<OrganizationSubscription, Long> {

    Optional<OrganizationSubscription> findByStripeSubscriptionId(String stripeId);

    Optional<OrganizationSubscription> findByOrganizationId(Long orgId);

    List<OrganizationSubscription> findByPaymentStatus(PaymentStatus status);
}