package com.sharkdom.partnerattribution.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EstimatedAcvResult {

    private BigDecimal estimatedAcv;

    private String confidenceLevel;

    private Integer sampleSize;

    private String calculationMethod;

    private String message;
}
