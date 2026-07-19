package com.sharkdom.dto;

import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.model.ai.RecordType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO for resetting integration and overlap frequency")
public class ResetIntegrationRequestDTO {

    @Schema(description = "Integration Type", example = "HUBSPOT")
    private IntegrationType integrationType;

    @Schema(description = "Record Type", example="CUSTOMER")
    private RecordType recordType;
}
