package com.sharkdom.partnertraining.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoursePerformanceResponse {

    private String courseTitle;
    private long enrolled;
    private long completed;
    private double avgCompletion;
}
