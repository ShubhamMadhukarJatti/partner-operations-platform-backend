package com.sharkdom.partnerattribution.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClosedWonDeal {

    private String dealId;

    private String industry;

    private String companySizeBand;

    private String geography;

    private String techStack;

    private String dealSource;

    private Boolean partnerInvolved;

    private String partnerName;

    private Double acv;
}
