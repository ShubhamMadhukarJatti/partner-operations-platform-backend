package com.sharkdom.partnerattribution.dto;

import lombok.Data;

import java.util.Map;

@Data
public class HubspotCompanyRawResponseDto {

    private String id;
    private Map<String, Object> properties;
    private String url;

}