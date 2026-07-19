package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.CourseLevel;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateCourseRequest {

    private String title;
    private String description;
    private String coverImageUrl;
    private CourseLevel level;
    private Integer durationMinutes;

    // Optional label update
    private Set<Long> labelIds;
}