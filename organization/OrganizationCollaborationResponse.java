package com.sharkdom.model.organization;

import com.sharkdom.constants.organization.OrganizationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationCollaborationResponse {
    Long organizationId;
    String organizationName;
    OrganizationStatus status;
    String logoUrl;
}
