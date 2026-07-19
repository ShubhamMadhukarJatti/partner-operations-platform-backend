package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.CourseLevel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseCardResponse {

    private Long courseId;
    private String title;
    private String description;
    private String coverImageUrl;

    private CourseLevel level;

    // UI badges / stats
    private Integer stageCount;
    private Integer durationMinutes;
    private Boolean published;

    // Future ready (shown in UI)
    private Integer completionPercentage; // nullable for now
}