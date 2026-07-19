package com.sharkdom.partnerattribution.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EstimatedAcvResponse {

    private String accountName;

    private Double estimatedAcv;

    private Double confidenceBand;

    private AcvCalculationMetadata metadata;
}
