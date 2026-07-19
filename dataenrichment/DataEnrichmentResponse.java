package com.sharkdom.dataenrichment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DataEnrichmentResponse {

    private Query query;

    @JsonProperty("company_url")
    private String companyUrl;

    private List<Employee> employees;

    @JsonProperty("total_fetched")
    private Integer totalFetched;

    @JsonProperty("total_matched")
    private Integer totalMatched;

    @JsonProperty("cost_usd")
    private Double costUsd;

    private Boolean cached;

    private String error;

    @JsonProperty("searched_at")
    private String searchedAt;
}