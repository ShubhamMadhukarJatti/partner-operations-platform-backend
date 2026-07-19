package com.sharkdom.partnertraining.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCourseCertificateUrlRequest {

    @NotBlank
    private String certificateUrl;
}