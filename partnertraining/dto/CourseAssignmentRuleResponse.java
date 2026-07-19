package com.sharkdom.partnertraining.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CourseAssignmentRuleResponse {

    private Long courseId;
    private List<String> tiers;
    private List<String> geographies;
    private List<String> programTypes;
}
