package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.UserCourseStatus;
import lombok.Data;

@Data
public class UserCourseStatusRequest {

    private String userId;
    private Long courseId;
    private UserCourseStatus status;
}
