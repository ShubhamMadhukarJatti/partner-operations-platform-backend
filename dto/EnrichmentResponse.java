package com.sharkdom.partnerattribution.dto;

import lombok.Data;

@Data
public class EnrichmentResponse {
    private boolean success;
    private EnrichmentData data;
}