package com.sharkdom.partnerattribution.dto;

import lombok.Data;

@Data
public class RecommendationDTO {

    private String type;
    private Double confidenceScore;
    private String message;

}
