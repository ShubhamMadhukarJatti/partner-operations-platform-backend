package com.sharkdom.partnertraining.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class DashboardResponse {
    private long totalCourses;
    private long totalPartners;
    private long activePartners;
    private Double avgReadinessPercentage;

    private List<PartnerReadinessDto> partnerReadiness;
    private List<CoursePerformanceResponse> coursePerformance;
}
