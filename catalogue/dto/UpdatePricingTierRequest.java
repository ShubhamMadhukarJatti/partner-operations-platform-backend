package com.sharkdom.entity.catalogue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Schema(description = "Request payload to update a pricing tier")
public class UpdatePricingTierRequest {

    @Schema(example = "Standard Plus")
    private String tierName;

    @Schema(example = "3499")
    private BigDecimal price;

    @Schema(example = "USD")
    private String currency;

    @Schema(example = "#FF9800")
    private String colorCode;

    @Schema(description = "Updated feature list",
            example = "[\"Cadence Operator\", \"AI Brain\"]")
    private Set<String> features;
}