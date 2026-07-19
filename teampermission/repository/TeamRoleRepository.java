package com.sharkdom.teampermission.repository;

import com.sharkdom.teampermission.entity.TeamRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRoleRepository extends JpaRepository<TeamRole, Long> {

    Optional<TeamRole> findByOrgIdAndName(Long orgId, String name);

    List<TeamRole> findAllByOrgId(Long orgId);

    boolean existsByOrgIdAndName(Long orgId, String name);

    Optional<TeamRole> findByOrgIdAndNameIgnoreCase(Long orgId, String name);

}

