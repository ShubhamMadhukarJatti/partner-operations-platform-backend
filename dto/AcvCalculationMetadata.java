package com.sharkdom.partnerattribution.dto;
import com.sharkdom.partnerattribution.enums.AcvCalculationStrategy;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcvCalculationMetadata {

    private AcvCalculationStrategy strategy;

    private Integer matchedDeals;

    private Double partnerUpliftMultiplier;

    private String notes;
}