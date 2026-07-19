package com.sharkdom.repository.stripe;

import com.sharkdom.entity.stripe.StripePayment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StripePaymentRepository extends JpaRepository<StripePayment, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StripePayment> findByPaymentIntentId(String paymentIntentId);

}
