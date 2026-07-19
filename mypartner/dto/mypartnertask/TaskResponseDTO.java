package com.sharkdom.mypartner.dto.mypartnertask;

import com.sharkdom.gtm.common.ProgressStage;
import com.sharkdom.gtm.common.Status;
import com.sharkdom.gtm.common.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "Response DTO for partner task")
public class TaskResponseDTO {

    private Long id;
    private String title;
    private Status status;
    private ProgressStage stage;
    private TargetType targetType;

    private Instant startDate;
    private Instant endDate;

    private String ownerId;
    private String note;

    private Long organizationId;
    private Long myPartnerId;
    private String userName;
}