package com.sharkdom.partnertraining.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardStatsResponse {
    private long assignedCourses;
    private long completedCourses;
    private long certificates;
    private int avgReadinessScore;
}
