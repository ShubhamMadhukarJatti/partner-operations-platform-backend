package com.sharkdom.entity.stripe;

import com.sharkdom.constants.stripe.StripeMode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "STRIPE_CHECKOUT_SESSIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripeCheckoutSessions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "SESSION_ID", unique = true, nullable = false)
    private String sessionId;

//    @ElementCollection
//    @CollectionTable(name = "STRIPE_CHECKOUT_SESSIONS_PAYMENT_METHOD_TYPES", joinColumns = @JoinColumn(name = "SESSION_ID"))
    @Column(name = "PAYMENT_METHOD_TYPES")
    private List<String> paymentMethodTypes;

    @Column(name = "CANCEL_URL")
    private String cancelUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "MODE")
    private StripeMode mode;

    @Column(name = "SUCCESS_URL")
    private String successUrl;

    private String paymentStatus;

    private String status;

    private String invoice;

    private LocalDate expiresAt;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "CUSTOMER_ID")
    private StripeCustomer customer;

    @OneToMany(mappedBy = "checkoutSession", fetch= FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<LineItemEntity> lineItems;

    @ManyToOne(fetch= FetchType.LAZY, cascade = CascadeType.ALL, optional = true)
    @JoinColumn(name = "subscription_id", nullable = true)
    private StripeSubscriptionData subscriptionData;

    @OneToMany(mappedBy = "checkoutSession", fetch= FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<StripeCoupon> coupons = new ArrayList<>();

    public void setLineItems(List<LineItemEntity> lineItems) {
        lineItems.forEach(lineItem -> lineItem.setCheckoutSession(this));
        this.lineItems = lineItems;
    }

    public void setCoupons(List<StripeCoupon> coupons) {
        coupons.forEach(coupon -> coupon.setCheckoutSession(this));
        this.coupons = coupons;
    }

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
