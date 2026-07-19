package com.sharkdom.partnerattribution.dto;

import lombok.Data;

@Data
public class AcvEstimationRequest {

    private String accountName;

    private String industry;

    private String companySizeBand;

    private String geography;

    private String techStack;

    private String dealSource;

    private Boolean partnerInvolved;

    private String partnerName;
}