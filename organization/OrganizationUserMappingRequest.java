
package com.sharkdom.entity.organization;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.constants.organization.OrgUserMappingRequestStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "organization_user_mapping_request")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class OrganizationUserMappingRequest extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long organizationId;
    private String userId;
    private OrgUserMappingRequestStatus status;
    @Column(unique = true)
    private String requestId;
    private long actionedByUserFk;

    public OrganizationUserMappingRequest(OrganizationUserMapping organizationUserMapping) {
        this.organizationId = organizationUserMapping.getOrganizationId();
        this.userId = organizationUserMapping.getUserId();
        this.status = OrgUserMappingRequestStatus.UNAPPROVED;
        this.requestId = UUID.randomUUID().toString();
    }

}

