package com.sharkdom.zoho.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ZohoDealSummaryDTO {
    @JsonProperty("Modified_Time")
    private String modifiedTime;

    @JsonProperty("Amount")
    private Double amount;

    @JsonProperty("Stage")
    private String stage;
}

