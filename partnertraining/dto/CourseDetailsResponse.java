package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.CourseLevel;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class CourseDetailsResponse {

    private Long courseId;
    private String title;
    private String description;
    private String coverImageUrl;
    private CourseLevel level;
    private Integer durationMinutes;
    private Boolean published;
    private Integer stageCount;
    private Set<String> labelNames;
    private List<StageDetailsResponse> stages;
}