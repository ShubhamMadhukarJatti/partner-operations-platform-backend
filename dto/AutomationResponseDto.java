package com.sharkdom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutomationResponseDto {

    @JsonProperty("Description of the Startup")
    private String descriptionOfStartup;

    @JsonProperty("Is_Agency")
    private String isAgency;

    @JsonProperty("Market Segment")
    private String marketSegment;

    @JsonProperty("One-line Description")
    private String oneLineDescription;

    @JsonProperty("Preferred Partnerships")
    private String preferredPartnerships;

    @JsonProperty("Sector of the Company")
    private String sectorOfCompany;

    @JsonProperty("Sector of the Ideal Company for Partnership")
    private String sectorOfIdealCompany;
}