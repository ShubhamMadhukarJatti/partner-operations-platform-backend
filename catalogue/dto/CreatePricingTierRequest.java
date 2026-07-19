package com.sharkdom.entity.catalogue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Schema(description = "Request payload to create a pricing tier")
public class CreatePricingTierRequest {

    @Schema(example = "Standard")
    private String tierName;

    @Schema(example = "2999")
    private BigDecimal price;

    @Schema(example = "USD")
    private String currency;

    @Schema(example = "#FFA500")
    private String colorCode;

    @Schema(description = "List of feature names",
            example = "[\"Cadence Operator\", \"Smart Calendar\", \"AI Brain\"]")
    private Set<String> features;
}