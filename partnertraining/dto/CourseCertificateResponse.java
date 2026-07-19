package com.sharkdom.partnertraining.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseCertificateResponse {

    private Long id;
    private Long courseId;
    private String userId;
    private Long orgId;
    private String certificateUrl;
}