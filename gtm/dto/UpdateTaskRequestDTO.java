package com.sharkdom.gtm.dto;

import com.sharkdom.gtm.common.ProgressStage;
import com.sharkdom.gtm.common.Status;
import com.sharkdom.gtm.common.TargetType;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateTaskRequestDTO {
    private String title;
    private Status status;
    private ProgressStage stage;
    private TargetType targetType;
    private Instant startDate;
    private Instant endDate;
    private String owner;
    private String note;
}
