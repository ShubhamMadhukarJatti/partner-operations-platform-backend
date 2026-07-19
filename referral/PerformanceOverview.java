package com.sharkdom.model.referral;

import lombok.Data;

@Data
public class PerformanceOverview {
    private Integer totalLeads;
    private Double revenueGenerated;
    private Integer businessFitScore;
    private String businessFitComment;
}
