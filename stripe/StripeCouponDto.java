package com.sharkdom.model.stripe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeCouponDto {

    private String couponName;

    private String couponId;

    private BigDecimal percentOff;

    private boolean isDeleted;

    private StripeCheckoutSessionsDto checkoutSession;

}
