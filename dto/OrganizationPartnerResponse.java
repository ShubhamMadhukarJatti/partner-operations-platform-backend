package com.sharkdom.dto;

import com.sharkdom.model.ai.PartnerDataPermissionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationPartnerResponse {
    private Long organizationId;
    private String organizationName;
    private String logoUrl;
    private Date assignmentDate;
    private PartnerDataPermissionResponse partnerDataPermissionResponse;
}