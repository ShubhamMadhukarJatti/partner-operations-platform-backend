package com.sharkdom.teampermission.repository;

import com.sharkdom.teampermission.entity.TeamMembers;
import com.sharkdom.teampermission.entity.TeamUserRolesAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMembers, Long> {
    Optional<TeamMembers> findByUserIdAndOrganizationId(String userId, Long organizationId);
    List<TeamMembers> findByUserId(String userId);
    List<TeamMembers> findByOrganizationIdAndRoles_Id(Long organizationId, Long roleId);
}
