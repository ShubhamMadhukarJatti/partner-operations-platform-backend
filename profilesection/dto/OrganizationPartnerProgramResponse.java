package com.sharkdom.profilesection.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrganizationPartnerProgramResponse {

    private Long id;
    private String programName;
    private Boolean isActive;
    private String programUrl;
    private List<String> benefits;
}