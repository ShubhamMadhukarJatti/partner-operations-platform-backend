package com.sharkdom.partnerattribution.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class CoSellRecommendationResponseDTO {

    private String accountId;
    private String accountName;
    private String stage;

    private RecommendationDTO recommendation;

    private List<InsightDTO> insights;

    private List<ActionDTO> actions;

    private Instant createdAt;

}