package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.UserCourseStatus;
import lombok.Data;

@Data
public class UserCoursePageRequest {

    private String userId;                  // mandatory
    private UserCourseStatus status;       // optional (ALL if null)
    private int page = 0;                  // default 0
    private int size = 8;                  // default 8
}
