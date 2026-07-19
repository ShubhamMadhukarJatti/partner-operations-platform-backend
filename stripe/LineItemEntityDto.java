package com.sharkdom.model.stripe;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineItemEntityDto {

    @JsonBackReference
    private Long id;

    @ToString.Exclude
    private StripeCheckoutSessionsDto checkoutSession;

    private PriceEntityDto price;  // Each line item refers to a Stripe Price

    private Long quantity;
}
