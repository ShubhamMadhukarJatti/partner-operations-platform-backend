package com.sharkdom.partnertraining.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseInsightsResponse {
    private Long totalPartner;
    private Double adoption;
    private Double completion;
    private Double avgReadiness;
}
