package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.StageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CreateStageRequest {

    @Schema(example = "Introduction to Product")
    private String title;

    @Schema(example = "CONTENT")
    private StageType type; // CONTENT / QUIZ
}