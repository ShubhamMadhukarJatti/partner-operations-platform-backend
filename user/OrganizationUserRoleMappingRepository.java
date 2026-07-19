package com.sharkdom.repository.user;

import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.entity.user.OrganizationUserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrganizationUserRoleMappingRepository extends JpaRepository<OrganizationUserRoleMapping, Long> {

    List<OrganizationUserRoleMapping> findByOrgUserMappingId(Long orgUserMappingId);

    @Query("SELECT r.role FROM OrganizationUserRoleMapping r WHERE r.orgUserMappingId = :mappingId")
    List<OrgUserRole> findRolesByOrgUserMappingId(@Param("mappingId") Long mappingId);

    @Query("SELECT r FROM OrganizationUserRoleMapping r WHERE r.orgUserMappingId = :mappingId AND r.role = :role")
    OrganizationUserRoleMapping findByOrgUserMappingIdAndRole(@Param("mappingId") Long mappingId,
                                                              @Param("role") OrgUserRole role);

    List<OrganizationUserRoleMapping> findByUserId(String userId);

    List<OrganizationUserRoleMapping> findByUserIdIn(List<String> userIds);

    @Modifying
    @Query(value = """
DELETE FROM organization_user_role_mapping
WHERE user_id = :userId
AND org_user_mapping_id = :orgUserMappingId
AND role NOT IN (:roles)
""", nativeQuery = true)
    int deleteExtraRolesNative(
            @Param("userId") String userId,
            @Param("orgUserMappingId") Long orgUserMappingId,
            @Param("roles") List<String> roles
    );


    List<OrganizationUserRoleMapping> findByUserIdAndOrgUserMappingId(
            String userId,
            Long orgUserMappingId
    );

    List<OrganizationUserRoleMapping> findByUserIdInAndRoleIn(
            List<String> userIds,
            List<OrgUserRole> roles
    );



}