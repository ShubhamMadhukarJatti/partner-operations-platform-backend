package com.sharkdom.teampermission.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.teampermission.repository.SharkdomRolesRepository;
import com.sharkdom.util.SharkdomApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharkdomRolesService {

    private final SharkdomRolesRepository sharkdomRolesRepository;

    @Transactional
    public SharkdomApiResponse<SharkdomRoles> createRole(String roleName, String description) {

        log.info("Request received to create role. roleName={}", roleName);

        // Validate role name
        if (roleName == null || roleName.trim().isEmpty()) {
            log.warn("Role creation failed due to empty role name");
            throw new ServiceException(ErrorMessages.SH189);
        }

        String normalizedRoleName = roleName.trim().toUpperCase();

        // Check duplicate
        if (sharkdomRolesRepository.existsByRoleName(normalizedRoleName)) {
            log.warn("Role creation failed. Role already exists. roleName={}", normalizedRoleName);
            throw new ServiceException(ErrorMessages.SH161, normalizedRoleName);
        }

        try {
            SharkdomRoles role = SharkdomRoles.builder()
                    .roleName(normalizedRoleName)
                    .description(description != null ? description.trim() : "")
                    .build();

            SharkdomRoles savedRole = sharkdomRolesRepository.save(role);

            log.info("Role created successfully. roleId={}, roleName={}",
                    savedRole.getId(), savedRole.getRoleName());

            return new SharkdomApiResponse<>(
                    true,
                    "Role created successfully",
                    savedRole
            );

        } catch (Exception ex) {
            log.error("Unexpected error occurred while creating role. roleName={}, error={}",
                    normalizedRoleName, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Transactional
    public SharkdomApiResponse<List<SharkdomRoles>> getAllRoles() {

        log.info("Fetching all roles");

        try {
            List<SharkdomRoles> roles = sharkdomRolesRepository.findAll();

            log.info("Successfully fetched roles. count={}", roles.size());

            return new SharkdomApiResponse<>(
                    true,
                    "Roles fetched successfully",
                    roles
            );

        } catch (Exception ex) {
            log.error("Unexpected error occurred while fetching roles. error={}",
                    ex.getMessage(), ex);
            throw ex;
        }
    }
}