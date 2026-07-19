package com.sharkdom.partnerattribution.dto;

import lombok.Data;

@Data
public class SharkdomEnrichmentHealthResponse {

    private String status;
    private String service;
    private String version;
    private String timestamp;
}
