package com.sharkdom.dto;

import lombok.Data;

@Data
public class MyPartnerMappingResponse {
    private Long partnerOrganizationId;
    private String organizationName;
    private String logoUrl;
    private Double overlapRate;
    private int aProspectOverlapCount;
    private int aCustomerOverlapCount;
    private int aOpportunityOverlapCount;
}
