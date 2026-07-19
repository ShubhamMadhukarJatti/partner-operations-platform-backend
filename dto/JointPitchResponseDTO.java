package com.sharkdom.partnerattribution.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JointPitchResponseDTO {
    private Long id;
    private Long orgId;
    private Long partnerOrgId;
    private String pitch;
    private String lastEditedBy;
    private LocalDateTime lastEditedAt;
    private String dealId;
}
