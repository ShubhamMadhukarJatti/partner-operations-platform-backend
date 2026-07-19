package com.sharkdom.model.stripe;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sharkdom.constants.stripe.StripePlanType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceEntityDto {

    @JsonBackReference
    private Long id;

    private String stripePriceId;  // "price_xxx"

    private StripePlanType planType;

    private List<StripeSubscriptionDataDto> subscriptions;

    private List<LineItemEntityDto> lineItems;

}
