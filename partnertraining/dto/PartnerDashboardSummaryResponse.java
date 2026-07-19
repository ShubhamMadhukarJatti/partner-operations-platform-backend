package com.sharkdom.partnertraining.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartnerDashboardSummaryResponse {

    private int assignedCourses;
    private int coursesCompleted;
    private int certificates;
    private int avgReadinessScore;
}
