package com.sharkdom.heathscore;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CoSellHealthResponse {

    private Double coSellWinProbability;
    private Integer coSellWinPoints;

    private Double directWinProbability;
    private Integer directWinPoints;

    private Integer partnerResponsivenessPoints;

    private Integer stageAlignmentPoints;

    private Integer timelineRiskPoints;

    private LocalDate projectedCloseDate;

    private Integer finalScore;

    private String healthStatus;
}