package com.sharkdom.entity.user;

import com.sharkdom.constants.organization.OrgUserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "organization_user_role_mapping",
        indexes = {@Index(columnList = "org_user_mapping_id", name = "idx_role_mapping_org_user")})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationUserRoleMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "org_user_mapping_id", nullable = false)
    private Long orgUserMappingId;

    @Column(name = "user_id")
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private OrgUserRole role;
}