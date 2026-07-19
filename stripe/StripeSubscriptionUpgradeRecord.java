package com.sharkdom.entity.stripe;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.OffsetDateTime;

@Entity
@Table(name = "STRIPE_SUBSCRIPTION_UPGRADE_RECORD")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StripeSubscriptionUpgradeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version
    private Long version;
    private String subscriptionId;
    private String currentPriceId;
    private String newPriceId;
    private String checkoutSessionId;
    private String status; // pending, completed, failed
    private Instant createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
