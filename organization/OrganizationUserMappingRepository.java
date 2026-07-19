package com.sharkdom.repository.organization;

import com.sharkdom.constants.organization.OrgUserMappingStatus;
import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organization.OrganizationUserMapping;
import com.sharkdom.model.organization.OrganizationUserMappingResponse;
import com.sharkdom.model.organization.OrganizationWithOrganizationMappingResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationUserMappingRepository extends JpaRepository<OrganizationUserMapping, Long> {


    @Query("select o from " + "OrganizationUserMapping m, "
            + "Organization o "
            + "where o.id = m.organizationId "
            + "and m.userId = :userId " +
            "and m.status = :status")
    List<Organization> findAllOrganizationsByUserId(String userId, OrgUserMappingStatus status);

    @Query("select o as organization, " +
            "m as organizationUserMapping" +
            " from " + "OrganizationUserMapping m, "
            + "Organization o "
            + "where o.id = m.organizationId "
            + "and m.userId = :userId")
    List<OrganizationWithOrganizationMappingResponse> findAllOrganizationsWithOrganizationMappingsByUserId(@Param("userId") String userId);

    @Query("select m as organizationUserMapping, u as user from " + "OrganizationUserMapping m, "
            + "User u "
            + "where u.userId = m.userId "
            + "and m.organizationId = :id")
    List<OrganizationUserMappingResponse> findAllByOrganizationId(long id);

    @Query("select m as organizationUserMapping, u as user from OrganizationUserMapping m, "
            + "User u "
            + "where u.userId = m.userId "
            + "and m.organizationId = :id " +
            " and m.status = :status ")
    List<OrganizationUserMappingResponse> findAllByOrganizationIdAndStatus(long id, OrgUserMappingStatus status);

    long countByOrganizationId(long organizationId);

    Optional<OrganizationUserMapping> findByOrganizationIdAndUserId(long organizationId, String userId);

    @Query("select m as organizationUserMapping, u as user from OrganizationUserMapping m, "
            + "User u "
            + "where u.userId = m.userId "
            + "and m.organizationId = :organizationId " +
            " and m.status = :status " +
            " and role = :role ")
    List<OrganizationUserMappingResponse> findAllByOrganizationIdAndRoleAndStatus(long organizationId, OrgUserRole role, OrgUserMappingStatus status);


    List<OrganizationUserMapping> findAllByUserId(String userId);

    @Query("select m as organizationUserMapping, u as user from OrganizationUserMapping m, "
            + "User u "
            + "where u.userId = m.userId "
            + "and m.organizationId = :id " +
            " and m.role = :role ")
    List<OrganizationUserMappingResponse> findByOrganizationIdAndRole(long id, OrgUserRole role);

    Optional<OrganizationUserMapping> findByOrganizationId(Long organisationId);

    @Query("SELECT m FROM OrganizationUserMapping m WHERE m.organizationId = :organizationId")
    List<OrganizationUserMapping> findByOrganizationsId(@Param("organizationId") Long organizationId);

    Optional<OrganizationUserMapping> findByUserId(String userId);

    @Query("select m as organizationUserMapping, u as user from OrganizationUserMapping m, "
            + "User u "
            + "where u.userId = m.userId "
            + "and m.organizationId = :id " +
            " and m.role = :role ")
    OrganizationUserMappingResponse findByOrganizationIdAndRoles(long id, OrgUserRole role);




}