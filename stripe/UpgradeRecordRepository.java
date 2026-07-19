package com.sharkdom.repository.stripe;

import com.sharkdom.entity.stripe.StripeSubscriptionUpgradeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UpgradeRecordRepository extends JpaRepository<StripeSubscriptionUpgradeRecord, Long> {
    Optional<StripeSubscriptionUpgradeRecord> findByCheckoutSessionId(String checkoutSessionId);
}
