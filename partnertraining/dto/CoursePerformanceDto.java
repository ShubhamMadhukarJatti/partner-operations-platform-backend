package com.sharkdom.partnertraining.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoursePerformanceDto {
    private String courseName;
    private int enrolled;
    private int completed;
    private int avgCompletionPercentage;
}
