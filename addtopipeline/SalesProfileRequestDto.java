package com.sharkdom.partnerattribution.addtopipeline;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SalesProfileRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Role is required")
    private String role;

    @NotNull(message = "Active deals is required")
    private Integer activeDeals;

    @NotNull(message = "Win rate is required")
    private Double winRate;

    @NotNull(message = "Average cycle days is required")
    private Integer avgCycleDays;

    @NotBlank(message = "Territory is required")
    private String territory;

    @NotNull(message = "Territory matched is required")
    private Boolean territoryMatched;

    @NotNull(message = "Organization Id is required")
    private Long orgId;
}