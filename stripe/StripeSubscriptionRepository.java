package com.sharkdom.repository.stripe;

import com.sharkdom.constants.stripe.StripeSubscriptionStatus;
import com.sharkdom.entity.stripe.StripeSubscriptionData;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StripeSubscriptionRepository extends JpaRepository<StripeSubscriptionData, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StripeSubscriptionData> findBySubscriptionId(String subscriptionId);

    boolean existsBySubscriptionId(String subscriptionId);

    @Query(value = "SELECT * FROM STRIPE_SUBSCRIPTION_DATA WHERE JSON_CONTAINS(ORGANIZATION_ID, CAST(:organizationId AS JSON))", nativeQuery = true)
    List<StripeSubscriptionData> findByOrganizationId(@Param("organizationId") Long organizationId);

    List<StripeSubscriptionData> findAllByEndOnAndStatus(LocalDate endOn, StripeSubscriptionStatus status);

    @Query(value = "SELECT * FROM STRIPE_SUBSCRIPTION_DATA ORDER BY id DESC LIMIT 10", nativeQuery = true)
    List<StripeSubscriptionData> findLast10SubscriptionsNative();


}
