package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.UserCourseStatus;
import lombok.Data;

@Data
public class UpdateAssignedCourseStatusRequest {

    private Long courseId;
    private Long assignedOrgId;
    private UserCourseStatus status;
}