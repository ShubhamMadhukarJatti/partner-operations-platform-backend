package com.sharkdom.entity.stripe;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "STRIPE_LINE_ITEMS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "CHECKOUT_SESSION_ID", referencedColumnName = "id")
    private StripeCheckoutSessions checkoutSession;  // Many line items per session

    @ManyToOne(cascade = CascadeType.ALL)
    @ToString.Exclude
    @JoinColumn(name = "PRICE_ID")
    private PriceEntity price;  // Each line item refers to a Stripe Price

    @Column(name = "QUANTITY")
    private Long quantity;
}
