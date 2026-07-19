package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.UserCourseStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatusResponse {

    private Long courseId;
    private UserCourseStatus status;
}