package com.sharkdom.mypartner.dto.mypartnertask;

import com.sharkdom.gtm.common.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "DTO for updating a partner task")
public class UpdateTaskDTO {

    private Long taskId;

    private String title;
    private Status status;
    private ProgressStage stage;
    private TargetType targetType;

    private Instant startDate;
    private Instant endDate;

    private String ownerId;
    private String note;
    private Long myPartnerId;
    private String userName;
}