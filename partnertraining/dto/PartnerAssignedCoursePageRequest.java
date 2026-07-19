package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.UserCourseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PartnerAssignedCoursePageRequest {

    @NotNull
    private Long assignedOrgId;

    private UserCourseStatus status;

    private int page = 0;

    private int size = 8;
}
