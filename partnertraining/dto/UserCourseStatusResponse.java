package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.UserCourseStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCourseStatusResponse {

    private Long courseId;
    private String userId;
    private UserCourseStatus status;
}
