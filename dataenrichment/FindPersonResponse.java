package com.sharkdom.dataenrichment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FindPersonResponse {

    private FindPersonQuery query;

    private List<PersonMatch> matches;

    @JsonProperty("total_candidates_found")
    private Integer totalCandidatesFound;

    @JsonProperty("total_scraped")
    private Integer totalScraped;

    @JsonProperty("cost_usd")
    private Double costUsd;

    private Boolean cached;

    private String error;

    @JsonProperty("searched_at")
    private String searchedAt;
}
