package com.sharkdom.profilesection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class OrganizationPartnerProgramRequest {

    @NotBlank
    private String programName;

    private Boolean isActive;

    private String programUrl;

    private List<String> benefits;
}