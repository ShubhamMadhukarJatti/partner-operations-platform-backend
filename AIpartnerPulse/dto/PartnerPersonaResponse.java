package com.sharkdom.AIpartnerPulse.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PartnerPersonaResponse {
    private List<Result> results;

    @Data
    public static class Result {
        private String url;
        private Division1 division_1;
        private Division2 division_2;
        private Division3 division_3;
    }

    @Data
    public static class Division1 {
        private Map<String, Double> ranked_thresholds;
    }

    @Data
    public static class Division2 {
        private String best_match;
        private Double threshold_score;
    }

    @Data
    public static class Division3 {
        private List<String> predicted_subsectors;
    }
}