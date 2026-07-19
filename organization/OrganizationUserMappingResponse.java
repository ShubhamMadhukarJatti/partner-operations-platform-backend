package com.sharkdom.model.organization;

import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.entity.organization.OrganizationUserMapping;
import com.sharkdom.entity.user.User;

import java.util.List;

public interface OrganizationUserMappingResponse {

    OrganizationUserMapping getOrganizationUserMapping();

    User getUser();

}
