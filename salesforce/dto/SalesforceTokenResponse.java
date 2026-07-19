package com.sharkdom.salesforce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SalesforceTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("instance_url") String instanceUrl,
        @JsonProperty("id") String id,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("issued_at") String issuedAt,
        @JsonProperty("signature") String signature
) { }