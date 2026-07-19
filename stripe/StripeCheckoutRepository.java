package com.sharkdom.repository.stripe;

import com.sharkdom.entity.stripe.StripeCheckoutSessions;
import com.sharkdom.entity.stripe.StripeSubscriptionData;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StripeCheckoutRepository extends JpaRepository<StripeCheckoutSessions,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StripeCheckoutSessions> findBySessionId(String sessionId);

    Optional<StripeCheckoutSessions> findBySubscriptionData(StripeSubscriptionData subscriptionData);

}
