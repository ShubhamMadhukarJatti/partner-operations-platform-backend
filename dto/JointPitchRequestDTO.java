package com.sharkdom.partnerattribution.dto;

import lombok.Data;

@Data
public class JointPitchRequestDTO {
    private Long partnerOrgId;
    private String pitch;
    private String dealId;
}
