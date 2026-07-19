package com.sharkdom.entity.stripe;

import com.sharkdom.constants.stripe.StripeSubscriptionStatus;
import com.sharkdom.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "STRIPE_SUBSCRIPTION_DATA")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripeSubscriptionData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "SUBSCRIPTION_ID", unique = true)
    private String subscriptionId;

    @Column(name = "TRAIL_PERIOD_DAYS")
    private Long trialPeriodDays;

    @Convert(converter = StringListConverter.class)
    @Column(name = "ORGANIZATION_ID", columnDefinition = "TEXT")
    private Set<Long> organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private StripeSubscriptionStatus status;

    @Column(name = "AMOUNT")
    private Long amount;
    private LocalDate endOn;
    private LocalDate startOn;
    private LocalDate cancelledOn;
    private String cancellationReason;
    private Integer runningMonth;
    private String additionalInfo;
    private String transactionId;
    private Long quantity;
    private Long seatLeft;
    private Long seatAssign;

    @OneToMany(mappedBy = "subscriptionData", fetch= FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<StripeCheckoutSessions> checkoutSession = new ArrayList<>();

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "CUSTOMER_ID")
    private StripeCustomer customer;

    @ManyToOne(cascade = CascadeType.ALL)
    @ToString.Exclude
    @JoinColumn(name = "PRICE_ID")
    private PriceEntity price;

    private String latestInvoice;

    public void addCheckoutSessions(StripeCheckoutSessions checkoutSessions) {
        checkoutSession.add(checkoutSessions);
        checkoutSessions.setSubscriptionData(this);
    }

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

}
