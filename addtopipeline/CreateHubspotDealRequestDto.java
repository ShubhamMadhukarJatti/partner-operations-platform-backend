package com.sharkdom.partnerattribution.addtopipeline;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class CreateHubspotDealRequestDto {

    /**
     * Vendor Organization Id
     */
    @NotNull(message = "Organization Id is required")
    private Long orgId;

    /**
     * HubSpot Deal Properties
     * Example:
     * dealname
     * dealstage
     * pipeline
     * amount
     * dealtype
     */
    @NotNull(message = "Properties are required")
    private Map<String, String> properties;
}