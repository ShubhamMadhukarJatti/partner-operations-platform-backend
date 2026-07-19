package com.sharkdom.profilesection.dto;

import com.sharkdom.profilesection.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrganizationCertificationResponse {

    private Long id;
    private String certificationName;
    private String verificationUrl;
    private VerificationStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;
    private String remarks;
    private Long certificateId;
}