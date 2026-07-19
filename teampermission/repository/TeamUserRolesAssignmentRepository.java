package com.sharkdom.teampermission.repository;

import com.sharkdom.teampermission.entity.TeamUserRoles;
import com.sharkdom.teampermission.entity.TeamUserRolesAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamUserRolesAssignmentRepository extends JpaRepository<TeamUserRolesAssignment, Long> {
    Optional<TeamUserRolesAssignment> findByUserIdAndRole(String userId, TeamUserRoles role);
    List<TeamUserRolesAssignment> findByUserId(String userId);
    Optional<TeamUserRolesAssignment> findByUserIdAndRoleId(String userId, Long roleId);
    boolean existsByUserIdAndRoleId(String userId, Long roleId);
    List<TeamUserRolesAssignment> findByRole(TeamUserRoles role);
    boolean existsByUserIdAndRole_IdAndOrganizationId(
            String userId,
            Long roleId,
            Long organizationId
    );
    boolean existsByUserIdAndRole_Id(String userId, Long roleId);

    List<TeamUserRolesAssignment> findByRoleIdAndOrganizationId(
            Long roleId,
            Long organizationId
    );
}
