package com.sharkdom.model.referral;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReferralData {
    private String dateRange;
    private String referralCode;
    private Long organizationId;
    private long uniqueImpressions;
    private double uniqueImpressionsChange;
    private long totalImpressions;
    private double totalImpressionsChange;
    private long leadsCount;
    private double leadsCountChange;
    private double conversionRate;
    private double conversionRateChange;
    private double revenue;
    private double revenueChange;
}
