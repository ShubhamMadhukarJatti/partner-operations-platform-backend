package com.sharkdom.mypartner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMyPartnerSegmentDTO {
    @JsonProperty("color")
    private String color;

    @JsonProperty("segment_name")
    private String segmentName;

    @JsonProperty("min_deals")
    private Integer minDeals;

    @JsonProperty("max_deals")
    private Integer maxDeals;

    @JsonProperty("active_co_marketing_campaign")
    private Boolean activeCoMarketingCampaign;
}
