package com.sharkdom.profilesection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CertificationRequest {

    @NotBlank(message = "Certification name is required")
    private String certificationName;

    @NotBlank(message = "Logo URL is required")
    private String logoUrl;
}