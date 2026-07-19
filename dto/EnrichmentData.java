package com.sharkdom.partnerattribution.dto;

import lombok.Data;
import java.util.List;

@Data
public class EnrichmentData {

    private String orgName;
    private String domain;
    private List<String> departments;
    private String search_identifier;
    private String search_identifier_source;
    private List<DecisionMaker> decision_makers;
    private String fetchedAt;
}