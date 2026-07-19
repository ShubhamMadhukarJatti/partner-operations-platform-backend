package com.sharkdom.model.referral;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReferralScriptCheckResponse {
    private boolean containsApiCall;
    private boolean containsExecuteImpressionFunction;
    private boolean containsSubmitFormFunction;
    private boolean containsReferralCode;
    private boolean success;
    private String error;
}
