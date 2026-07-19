package com.sharkdom.model.payment;

import com.sharkdom.constants.PlanType;
import lombok.Data;

@Data
public class StripeCheckoutRequest {
    private String currency;
    private Double amount;
    private PlanType planType;
    private Long organizationId;
}
