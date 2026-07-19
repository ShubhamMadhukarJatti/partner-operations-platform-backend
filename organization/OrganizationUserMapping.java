package com.sharkdom.entity.organization;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.constants.organization.OrgUserMappingStatus;
import com.sharkdom.constants.organization.OrgUserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization_user_mapping", indexes = {@Index(columnList = "organizationId", name = "org_user_orgId"),
        @Index(columnList = "userId", name = "org_user_userId")})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString
public class OrganizationUserMapping extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Schema(example = "1", description = "id field of organization entity")
    @Column(nullable = false)
    private Long organizationId;
    @Column(nullable = false)
    private String userId;
    @Schema(example = "CEO/CTO")
    private String designation;
    @Column(nullable = false)
    @Schema(name = "role", example = "ADMIN", description = "Mandatory field, First user should be ADMIN")
    private OrgUserRole role;
    @Schema(description = "Status of mapping of user with Organization, first user should have ACTIVE, other new users should have PENDING")
    private OrgUserMappingStatus status;
    private long approvedByUserFk;
    @Schema(description = "Id of Signatory table if the user is signatory or director")
    private long signatoryId;


}