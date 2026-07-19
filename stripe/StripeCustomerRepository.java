package com.sharkdom.repository.stripe;

import com.sharkdom.entity.stripe.StripeCustomer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StripeCustomerRepository extends JpaRepository<StripeCustomer,Long> {

    boolean existsByCustomerId(String customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StripeCustomer> findByCustomerId(String customerId);

    boolean existsByFirebaseUserId(String firebaseUserId);

    Optional<StripeCustomer> findByFirebaseUserId(String firebaseUserId);
}
