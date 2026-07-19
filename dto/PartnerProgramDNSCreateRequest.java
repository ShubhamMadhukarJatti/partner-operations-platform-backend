package com.sharkdom.dto;

import lombok.Data;

@Data
public class PartnerProgramDNSCreateRequest {
    private String customDomain;
    private String targetHost;
}