package com.sharkdom.entity.stripe;

import com.sharkdom.constants.stripe.StripePlanType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "STRIPE_PRICE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "STRIPE_PRICE_ID", nullable = false)
    private String stripePriceId;  // "price_xxx"

    @Enumerated(EnumType.STRING)
    @Column(name = "PLAN_TYPE", length = 600)
    private StripePlanType planType;

    @OneToMany(mappedBy = "price", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<StripeSubscriptionData> subscriptions;

    @OneToMany(mappedBy = "price", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineItemEntity> lineItems;


}
