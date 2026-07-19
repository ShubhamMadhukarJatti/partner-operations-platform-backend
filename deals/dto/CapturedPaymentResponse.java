package com.sharkdom.deals.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class CapturedPaymentResponse {

    private String paymentId;
    private Double amount;
    private String currency;
    private String status;
    private String method;
    private String bank;
    private String contact;
    private String email;
    private Date timestamp;
    private String partnerName;
    private String partnerId;
}
