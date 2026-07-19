package com.sharkdom.entity.stripe;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "STRIPE_CARD_DETAIL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripeCardDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String last4;
    private Long expMonth;
    private Long expYear;
    private String country;
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "CUSTOMER_ID")
    private StripeCustomer customer;
}
