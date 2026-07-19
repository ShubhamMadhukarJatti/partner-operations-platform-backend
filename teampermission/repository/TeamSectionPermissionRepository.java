package com.sharkdom.teampermission.repository;

import com.sharkdom.teampermission.entity.TeamSectionPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TeamSectionPermissionRepository extends JpaRepository<TeamSectionPermission, Long> {
    List<TeamSectionPermission> findByCodeInIgnoreCase(Collection<String> codes);
}
