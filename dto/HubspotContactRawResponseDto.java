package com.sharkdom.partnerattribution.dto;

import lombok.Data;

import java.util.Map;

@Data
public class HubspotContactRawResponseDto {

    private String id;
    private Map<String, Object> properties;

}