package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.StageType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseStageResponse {

    private Long stageId;
    private String title;
    private StageType type;
    private Integer order;
    private boolean isContentCreated;
}