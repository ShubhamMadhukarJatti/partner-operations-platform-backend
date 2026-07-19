package com.sharkdom.partnertraining.dto;

import lombok.Data;

@Data
public class AssignCourseRequest {

    private Long courseId;
    private Long assignedOrgId;
}
