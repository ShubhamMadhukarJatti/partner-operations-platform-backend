package com.sharkdom.profilesection.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizationResourceResponse {

    private Long id;
    private String title;
    private String type;
    private String source;
    private String url;
    private String description;
}