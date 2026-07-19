package com.sharkdom.entity.stripe;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "STRIPE_CUSTOMER_DB")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripeCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "CUSTOMER_ID", unique = true, nullable = false)
    private String customerId;

    @Column(name = "ORGANIZATION_ID")
    private Set<Long> organizationId;

    @Column(name = "FIREBASE_USER_ID", unique = true)
    private String firebaseUserId;

    @Column(name = "CUSTOMER_NAME")
    private String customerName;

    @Column(name = "CUSTOMER_EMAIL")
    private String customerEmail;

    @Column(name = "CUSTOMER_PHONE_NUMBER")
    private String customerPhoneNumber;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StripeCheckoutSessions> checkoutSessions;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<StripeSubscriptionData> subscriptions;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    List<StripeCardDetail> cardDetails = new ArrayList<>();

    public void addCard(StripeCardDetail cardDetail) {
        cardDetails.add(cardDetail);
        cardDetail.setCustomer(this);
    }

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

}
