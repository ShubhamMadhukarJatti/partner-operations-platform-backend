package com.sharkdom.profilesection.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrganizationProfileResponse {

    private List<OrganizationCertificationResponse> certifications;
    private List<OrganizationResourceResponse> resources;
    private OrganizationPartnerProgramResponse partnerProgram;
}