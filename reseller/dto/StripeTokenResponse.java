package com.sharkdom.reseller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StripeTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("livemode")
    private boolean livemode;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("stripe_publishable_key")
    private String stripePublishableKey;

    @JsonProperty("stripe_user_id")
    private String stripeUserId;

    @JsonProperty("scope")
    private String scope;
}