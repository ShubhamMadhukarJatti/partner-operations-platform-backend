package com.sharkdom.entity.stripe;

import com.sharkdom.constants.stripe.StripePaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripePayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(unique = true)
    private String paymentIntentId;
    private String chargeId;
    private String customerId;
    private Double amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private StripePaymentStatus status;

    private String paymentMethod;
    private String receiptUrl;
    private String failureMessage;
    private String declineCode;
    private String adviceCode;

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

}


