package com.sharkdom.dto;

import lombok.Data;

import java.util.Date;

@Data
public class MyPartnerMappingReportStatusResponse {
    private String organizationName;
    private String logoUrl;
    private Long organizationId;
    private Date createdAt;
    private String yourMatrix;
    private String partnerMatrix;
    private String overlapCount;
}
