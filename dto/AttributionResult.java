package com.sharkdom.partnerattribution.dto;

import com.sharkdom.partnerattribution.enums.AttributionTier;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttributionResult {

    private AttributionTier tier;

    private double baseTierValue;

    private double decayFactor;

    private double attributionStrength;

    private String reason;
}