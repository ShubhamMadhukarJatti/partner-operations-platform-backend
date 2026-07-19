package com.sharkdom.model.stripe;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sharkdom.constants.stripe.StripeMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeCheckoutSessionsDto {

    private Long id;

    private String sessionId;

    private List<String> paymentMethodTypes;

    @URL
    private String cancelUrl;

    private StripeMode mode;

    @URL
    private String successUrl;

    @ToString.Exclude
    private StripeCustomerDto customer;

    @ToString.Exclude
    private StripeSubscriptionDataDto subscriptionData;

//    @ToString.Exclude
    private List<LineItemEntityDto> lineItems;

    @URL
    private String checkoutUrl;

    private String paymentStatus;

    private String status;

    private String invoice;

    private LocalDate expiresAt;

    private List<StripeCouponDto> coupons;

}
