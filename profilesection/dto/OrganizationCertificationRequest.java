package com.sharkdom.profilesection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrganizationCertificationRequest {

    @NotBlank
    private String certificationName;

    private String verificationUrl;

    private Long certificationId;

}