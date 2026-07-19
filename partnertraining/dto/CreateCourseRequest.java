package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class CreateCourseRequest {

    @NotBlank
    private String title;

    private String description;

    private String coverImageUrl;

    @NotNull
    private CourseLevel level;

    private Integer durationMinutes;

    // Label IDs selected from dropdown
    private Set<Long> labelIds;
}
