package com.sharkdom.partnerattribution.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SharkdomEnrichmentInfoResponse {

    private String service;
    private String version;
    private String description;
    private Map<String, String> endpoints;
}