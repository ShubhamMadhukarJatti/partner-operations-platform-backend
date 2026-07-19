package com.sharkdom.entity.stripe;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "STRIPE_COUPON")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripeCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String couponName;

    @Column(unique = true)
    private String couponId;

    private BigDecimal percentOff;

    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @JoinColumn(name = "CHECKOUT_SESSION_ID")
    private StripeCheckoutSessions checkoutSession;

}
