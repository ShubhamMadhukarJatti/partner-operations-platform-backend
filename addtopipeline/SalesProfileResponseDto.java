package com.sharkdom.partnerattribution.addtopipeline;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalesProfileResponseDto {

    private Long id;

    private String name;

    private String role;

    private Integer activeDeals;

    private Double winRate;

    private Integer avgCycleDays;

    private String territory;

    private Boolean territoryMatched;

    private Long orgId;
}
