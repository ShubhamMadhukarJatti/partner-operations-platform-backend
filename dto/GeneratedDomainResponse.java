package com.sharkdom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GeneratedDomainResponse {
    private String extractedSubdomain;
    private String targetHost;
    private String recordType;
}