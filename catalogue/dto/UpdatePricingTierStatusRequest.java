package com.sharkdom.entity.catalogue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request to update pricing tier active status")
public class UpdatePricingTierStatusRequest {

    @Schema(
            description = "Status of the pricing tier",
            example = "true"
    )
    private Boolean active;
}