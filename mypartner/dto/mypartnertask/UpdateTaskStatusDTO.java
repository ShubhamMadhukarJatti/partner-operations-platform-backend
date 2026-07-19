package com.sharkdom.mypartner.dto.mypartnertask;

import com.sharkdom.gtm.common.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO for updating task status")
public class UpdateTaskStatusDTO {

    @Schema(description = "Task ID", example = "1")
    private Long taskId;

    @Schema(description = "New Status", example = "IN_PROGRESS")
    private Status status;
}