package com.sharkdom.model.stripe;

import lombok.Data;

import java.util.List;

@Data
public class PayoutRequirmentResponse {
    private List<String> currently_due;
    private List<String> errors;
    private List<String> past_due;
    private List<String> pending_verification;
}
