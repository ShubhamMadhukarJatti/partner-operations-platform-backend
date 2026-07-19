package com.sharkdom.teampermission.repository;

import com.sharkdom.teampermission.entity.TeamUserRoles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamUserRolesRepository extends JpaRepository<TeamUserRoles, Long> {
    Optional<TeamUserRoles> findByNameAndOrganizationId(String name, Long organizationId);
    Optional<TeamUserRoles> findByNameAndOrganizationIdIsNull(String name);
    List<TeamUserRoles> findByNameInAndOrganizationIdIsNull(List<String> names);
    List<TeamUserRoles> findByNameInAndOrganizationId(List<String> names, Long organizationId);
    Optional<TeamUserRoles> findByOrganizationIdAndName(Long organizationId, String name);
}