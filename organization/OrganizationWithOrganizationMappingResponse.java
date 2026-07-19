package com.sharkdom.model.organization;

import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organization.OrganizationUserMapping;

public interface OrganizationWithOrganizationMappingResponse {

    OrganizationUserMapping getOrganizationUserMapping();

    Organization getOrganization();

}
