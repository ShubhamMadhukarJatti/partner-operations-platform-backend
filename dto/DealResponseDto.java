package com.sharkdom.dto;

import lombok.Data;
import java.util.Date;
import java.util.Map;

@Data
public class DealResponseDto {
    private String customerAccountName;
    private String dealId;
    private String dealCode;
    private String website;
    private String headQuarterLocation;
    private Integer estimatedAcv;
    private Integer expectedClosingTime;
    private String currentSolution;
    private String requirements;
    private String customFields;
    private Map<String, Object> customFieldsMap; // parsed version
    private String dealStage;
    private String source;
    private Boolean isApproved;
    private Long dealerOrgId;
    private Long vendorOrgId;
    private Long dealProtectionPeriod;
    private Boolean isSent;
    private String dealStatus;
    private String dealSize;
    private String hotspotDealId;
    private Date lastUpdatedTimestamp;
    private String lastActivity;
    private String pointOfContact;
    private String salesforceDealId;
    private String zohoDealId;
    private String provider;

    // Extra field (from Organization)
    private String orgName;
}

