package com.sharkdom.service.partenerDeals.hubspot.dto;

import lombok.Data;

@Data
public class HubSpotDealPropertyRequest {
    private String name;
    private String label;
    private String type;
    private String fieldType;
    private String groupName;
    private String description;
    private boolean hidden;
}