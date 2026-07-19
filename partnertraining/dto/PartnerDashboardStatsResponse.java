package com.sharkdom.partnertraining.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartnerDashboardStatsResponse {

    private long assignedCourses;
    private long completedCourses;
    private long certificates;
    private int avgReadinessPercentage;
}

