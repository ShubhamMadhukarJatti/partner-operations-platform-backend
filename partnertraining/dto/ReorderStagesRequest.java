package com.sharkdom.partnertraining.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ReorderStagesRequest {

    @Schema(
            description = "Stage IDs in the new order",
            example = "[5, 2, 7, 3]"
    )
    private List<Long> stageIds;
}