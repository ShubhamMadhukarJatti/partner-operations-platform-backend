package com.sharkdom.partnerattribution.dto;

import lombok.Data;

@Data
public class ActivityLogRequestDTO {
    private Long partnerOrgId;
    private String title;
    private String description;
    private String activityType;
    private String userName;
    private String dealId;
}