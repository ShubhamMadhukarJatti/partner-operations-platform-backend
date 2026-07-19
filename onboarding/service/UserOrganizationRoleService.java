package com.sharkdom.onboarding.service;

import com.sharkdom.onboarding.entity.UserOrganizationRole;
import com.sharkdom.onboarding.model.UserOrganizationRoleDto;
import com.sharkdom.onboarding.repository.UserOrganizationRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserOrganizationRoleService {

    private final UserOrganizationRoleRepository repository;

    public UserOrganizationRoleDto createOrUpdate(UserOrganizationRoleDto dto) {

        UserOrganizationRole entity = repository.findByUserId(dto.getUserId())
                .orElse(UserOrganizationRole.builder()
                        .userId(dto.getUserId())
                        .orgId(dto.getOrgId())
                        .isVendor(false)
                        .isPartner(false)
                        .build());

        if (dto.getIsVendor() != null)
            entity.setIsVendor(dto.getIsVendor());

        if (dto.getIsPartner() != null)
            entity.setIsPartner(dto.getIsPartner());

        repository.save(entity);

        return UserOrganizationRoleDto.builder()
                .userId(entity.getUserId())
                .orgId(entity.getOrgId())
                .isVendor(entity.getIsVendor())
                .isPartner(entity.getIsPartner())
                .build();
    }

    public UserOrganizationRoleDto getByUserId(String userId) {

        UserOrganizationRole entity = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserOrganizationRoleDto.builder()
                .userId(entity.getUserId())
                .orgId(entity.getOrgId())
                .isVendor(entity.getIsVendor())
                .isPartner(entity.getIsPartner())
                .build();
    }

    public UserOrganizationRoleDto getByUserViewById(String userId) {

        UserOrganizationRole entity = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserOrganizationRoleDto.builder()
                .userId(entity.getUserId())
                .orgId(entity.getOrgId())
                .isVendor(entity.getIsVendor())
                .isPartner(entity.getIsPartner())
                .build();
    }


    public UserOrganizationRoleDto toggleVendorPartner(String userId) {

        UserOrganizationRole entity = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(entity.getIsPartner())) {
            entity.setIsPartner(false);
            entity.setIsVendor(true);
        } else {
            entity.setIsVendor(false);
            entity.setIsPartner(true);
        }

        repository.save(entity);

        return UserOrganizationRoleDto.builder()
                .userId(entity.getUserId())
                .orgId(entity.getOrgId())
                .isVendor(entity.getIsVendor())
                .isPartner(entity.getIsPartner())
                .build();
    }
}
