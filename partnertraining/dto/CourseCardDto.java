package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.UserCourseStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseCardDto {

    private Long courseId;
    private String title;
    private String thumbnailUrl;

    private int modules;
    private int durationInMinutes;
    private String level;

    private UserCourseStatus status;
    private int progressPercentage;
}
