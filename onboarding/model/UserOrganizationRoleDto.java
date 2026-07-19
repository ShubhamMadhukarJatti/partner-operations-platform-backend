package com.sharkdom.onboarding.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrganizationRoleDto {

    private String userId;
    private Long orgId;
    private Boolean isVendor;
    private Boolean isPartner;
}