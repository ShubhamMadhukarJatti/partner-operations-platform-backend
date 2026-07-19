package com.sharkdom.onboarding.repository;

import com.sharkdom.onboarding.entity.UserOrganizationRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOrganizationRoleRepository
        extends JpaRepository<UserOrganizationRole, Long> {

    Optional<UserOrganizationRole> findByUserId(String userId);
}
