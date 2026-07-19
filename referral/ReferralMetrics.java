package com.sharkdom.model.referral;

import lombok.Data;

@Data
public class ReferralMetrics {
    private Integer leads;
    private Long conversions;
    private Double conversionRate;
}
