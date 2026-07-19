package com.sharkdom.partnerattribution.dto;

import com.sharkdom.partnerattribution.enums.Priority;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AgreedNextStepResponseDto {

    private Long id;
    private Long orgId;
    private String title;
    private String description;
    private String owner;
    private Priority priority;
    private LocalDate dueDate;
    private Boolean isCompleted;
    private String dealId;
}
