package com.sharkdom.dataenrichment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DataEnrichmentRequest {

    private String company;

    private String designation;

    private String location;

    private Integer limit;

    @JsonProperty("company_url")
    private String companyUrl;

    @JsonProperty("strict_current")
    private Boolean strictCurrent;

    @JsonProperty("user_id")
    private String userId;
}