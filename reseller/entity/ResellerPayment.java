package com.sharkdom.reseller.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "t_vendor_payment"
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResellerPayment extends BaseEntity {

    @Column(name = "reseller_id", nullable = false)
    private Long resellerId;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "vendor_org_id", nullable = false)
    private Long vendorOrgId;

    @Column(name = "stripe_session_id", columnDefinition = "TEXT")
    private String stripeSessionId;

    @Column(name = "checkout_url", columnDefinition = "TEXT")
    private String checkoutUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    private PaymentStatus paymentStatus;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "accountID", nullable = false)
    private String resellerAccountID;

    @Column(name= "customer_email", nullable = false)
    private String customerEmail;

}
