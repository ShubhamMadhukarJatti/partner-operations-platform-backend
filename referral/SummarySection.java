package com.sharkdom.model.referral;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummarySection {
    private MetricWithGrowth totalPartners;
    private MetricWithGrowth averagePerformance;
    private MetricWithGrowth totalLeads;
}
