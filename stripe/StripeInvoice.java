package com.sharkdom.entity.stripe;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "STRIPE_INVOICE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripeInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(unique = true)
    private String invoiceId;

    private String subscriptionId;

    private String customerId;

    private String invoicePdfUrl;

}
