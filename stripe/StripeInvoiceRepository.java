package com.sharkdom.repository.stripe;

import com.sharkdom.entity.stripe.StripeInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StripeInvoiceRepository extends JpaRepository<StripeInvoice, Long> {

    Optional<StripeInvoice> findByInvoiceId(String invoiceId);

    List<StripeInvoice> findAllBySubscriptionId(String subscriptionId);

    List<StripeInvoice> findBySubscriptionId(String subscriptionId);

}
