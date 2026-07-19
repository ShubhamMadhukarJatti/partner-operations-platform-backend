package com.sharkdom.teampermission.repository;

import com.sharkdom.teampermission.service.SharkdomRoles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SharkdomRolesRepository extends JpaRepository<SharkdomRoles, Long> {

    Optional<SharkdomRoles> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);
}