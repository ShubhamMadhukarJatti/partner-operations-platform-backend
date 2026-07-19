package com.sharkdom.partnertraining.dto;

import lombok.Data;

import java.util.List;

@Data
public class CourseAssignmentRuleRequest {

    private List<String> tiers;
    private List<String> geographies;
    private List<String> programTypes;
}

