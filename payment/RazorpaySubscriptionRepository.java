package com.sharkdom.repository.payment;

import com.sharkdom.entity.payment.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RazorpaySubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    SubscriptionEntity findBySubscriptionId(String subscriptionId);
}
