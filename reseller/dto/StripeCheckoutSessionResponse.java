package com.sharkdom.reseller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StripeCheckoutSessionResponse {

    private String id;
    private String url;

}
