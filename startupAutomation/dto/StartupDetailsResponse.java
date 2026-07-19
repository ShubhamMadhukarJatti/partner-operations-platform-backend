package com.sharkdom.startupAutomation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StartupDetailsResponse {
    @JsonProperty("Description of the Startup")
    private String descriptionOfTheStartup;

    @JsonProperty("Is_Agency")
    private String isAgency;

    @JsonProperty("Market Segment")
    private String marketSegment;

    @JsonProperty("One-line Description")
    private String oneLineDescription;

    @JsonProperty("Preferred Partnerships")
    private String preferredPartnerships;

    @JsonProperty("Sector of the Company")
    private String sectorOfTheCompany;

    @JsonProperty("Sector of the Ideal Company for Partnership")
    private String sectorOfTheIdealCompanyForPartnership;
}
