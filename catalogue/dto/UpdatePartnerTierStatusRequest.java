package com.sharkdom.entity.catalogue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        name = "UpdatePartnerTierStatusRequest",
        description = "Request payload to enable or disable a Partner Tier"
)
public class UpdatePartnerTierStatusRequest {

    @Schema(
            description = "Tier active status",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean active;
}
