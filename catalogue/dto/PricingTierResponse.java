package com.sharkdom.entity.catalogue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@Schema(description = "Pricing Tier response")
public class PricingTierResponse {

    private Long id;
    private String tierName;
    private BigDecimal price;
    private String currency;
    private String colorCode;
    private Set<String> features;
    private boolean isActive;
}