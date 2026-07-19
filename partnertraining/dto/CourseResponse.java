package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.CourseLevel;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private String coverImageUrl;
    private CourseLevel level;
    private Integer durationMinutes;
    private Boolean published;

    private Set<String> labels;

    private Set<Long> labelIds;
    private Set<String> labelNames;
}