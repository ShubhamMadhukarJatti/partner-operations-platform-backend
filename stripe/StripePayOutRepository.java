package com.sharkdom.repository.stripe;

import com.sharkdom.entity.stripe.StripePayOutBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StripePayOutRepository extends JpaRepository<StripePayOutBankAccount, Long> {
    Optional<StripePayOutBankAccount> findByAccountAndRoutingNumber(String accountNumber, String routingNumber);
}
