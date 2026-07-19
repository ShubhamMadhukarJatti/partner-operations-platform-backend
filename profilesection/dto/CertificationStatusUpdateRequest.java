package com.sharkdom.profilesection.dto;

import com.sharkdom.profilesection.enums.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CertificationStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private VerificationStatus status;

    private String remarks;
}