package com.sharkdom.entity.paymenttracking;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "razorpay_tracking_test")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayTrackingTestEntity extends BaseEntity {
    private String affiliateCode;
    private String organizationCode;
    private Long organizationId;
    private String accountId;
    private String eventType;
    private String paymentId;
    private String orderId;
    private int amount;
    private String currency;
    private String status;
    private String method;
    private String bank;
    private String contact;
    private String email;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String payload;
}

