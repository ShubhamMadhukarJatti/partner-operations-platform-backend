package com.sharkdom.model.paymenttracking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RazorpayPaymentDetailsDto {

    private String referralCode;
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
}
