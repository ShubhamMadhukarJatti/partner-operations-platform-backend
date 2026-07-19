package com.sharkdom.gtm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.gtm.common.ProgressStage;
import com.sharkdom.gtm.common.Status;
import com.sharkdom.gtm.common.TargetType;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateTaskRequestDTO {

    @JsonProperty("task_title")
    private String title;

    @JsonProperty("status")
    private Status status;

    @JsonProperty("stage")
    private ProgressStage stage;

    @JsonProperty("target_type")
    private TargetType targetType;

    @JsonProperty("start_date")
    private Instant startDate;

    @JsonProperty("end_date")
    private Instant endDate;

    @JsonProperty("owner_id")
    private String owner;

    @JsonProperty("note")
    private String note;

    @JsonProperty("organization_id")
    private Long organizationId;

    @JsonProperty("external_partner_id")
    private Long externalPartnerId;

    @JsonProperty("external_partner_code")
    private String externalPartnerCode;
}