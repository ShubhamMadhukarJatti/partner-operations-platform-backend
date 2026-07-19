package com.sharkdom.model.stripe;

import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
public class StripePayoutAccountResponse {
    private String id;
    private String object;
    private String account;
    private String account_holder_name;
    private String account_holder_type;
    private String account_type;
    private List<String> available_payout_methods;
    private String bank_name;
    private String country;
    private String currency;
    private boolean default_for_currency;
    private String fingerprint;
    private PayoutRequirmentResponse future_requirements;
    private PayoutRequirmentResponse requirements;
    private String last4;
    private Map<String, String> metadata;
    private String routing_number;
    private String status;
}
