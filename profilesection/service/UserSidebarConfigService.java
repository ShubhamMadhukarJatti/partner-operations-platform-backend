package com.sharkdom.profilesection.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.profilesection.dto.UserSidebarConfigDto;
import com.sharkdom.profilesection.entity.UserSidebarConfig;
import com.sharkdom.profilesection.repository.UserSidebarConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSidebarConfigService {

    private final UserSidebarConfigRepository repository;
    private final ObjectMapper objectMapper;

    public UserSidebarConfigDto create(UserSidebarConfigDto dto) {
        try {

            UserSidebarConfig entity = UserSidebarConfig.builder()
                    .userId(dto.getUserId())
                    .sidebarItemHrefs(objectMapper.writeValueAsString(dto.getSidebarItemHrefs()))
                    .pinnedItemHrefs(objectMapper.writeValueAsString(dto.getPinnedItemHrefs()))
                    .openNestedItems(objectMapper.writeValueAsString(dto.getOpenNestedItems()))
                    .isCollapsed(dto.getIsCollapsed())
                    .isPartnerView(dto.getIsPartnerView())
                    .isVendorView(dto.getIsVendorView())
                    .build();

            UserSidebarConfig saved = repository.save(entity);

            log.info("Saved entity: {}", saved);

            return dto;

        } catch (Exception e) {
            log.error("Error saving sidebar config", e);
            throw new RuntimeException(e);
        }
    }

    public UserSidebarConfigDto update(UserSidebarConfigDto dto) {

        try {

            log.info("Updating sidebar config for userId={}, partnerView={}, vendorView={}",
                    dto.getUserId(),
                    dto.getIsPartnerView(),
                    dto.getIsVendorView());

            UserSidebarConfig entity = null;

            if (Boolean.TRUE.equals(dto.getIsPartnerView())) {

                entity = repository
                        .findTopByUserIdAndIsPartnerViewTrueOrderByIdDesc(dto.getUserId())
                        .orElse(null);

            } else if (Boolean.TRUE.equals(dto.getIsVendorView())) {

                entity = repository
                        .findTopByUserIdAndIsVendorViewTrueOrderByIdDesc(dto.getUserId())
                        .orElse(null);
            }

            // Create new config if not found
            if (entity == null) {

                log.info("No existing sidebar config found. Creating new one.");

                entity = UserSidebarConfig.builder()
                        .userId(dto.getUserId())
                        .isPartnerView(dto.getIsPartnerView())
                        .isVendorView(dto.getIsVendorView())
                        .build();
            }

            // Update only if values are NOT NULL

            if (dto.getPinnedItemHrefs() != null) {
                entity.setPinnedItemHrefs(objectMapper.writeValueAsString(dto.getPinnedItemHrefs()));
            }

            if (dto.getSidebarItemHrefs() != null) {
                entity.setSidebarItemHrefs(objectMapper.writeValueAsString(dto.getSidebarItemHrefs()));
            }

            if (dto.getOpenNestedItems() != null) {
                entity.setOpenNestedItems(objectMapper.writeValueAsString(dto.getOpenNestedItems()));
            }

            if (dto.getIsCollapsed() != null) {
                entity.setIsCollapsed(dto.getIsCollapsed());
            }

            UserSidebarConfig saved = repository.save(entity);

            log.info("Sidebar config saved successfully. id={}", saved.getId());

            return dto;

        } catch (Exception e) {

            log.error("Error updating sidebar config for userId={}", dto.getUserId(), e);
            throw new RuntimeException("Failed to update sidebar config", e);
        }
    }

    public UserSidebarConfigDto getByUserIdAndRole(String userId, Boolean isPartner, Boolean isVendor) {

        UserSidebarConfig entity = null;

        if (Boolean.TRUE.equals(isPartner)) {

            entity = repository
                    .findTopByUserIdAndIsPartnerViewTrueOrderByIdDesc(userId)
                    .orElse(null);

        } else if (Boolean.TRUE.equals(isVendor)) {

            entity = repository
                    .findTopByUserIdAndIsVendorViewTrueOrderByIdDesc(userId)
                    .orElse(null);
        }

        if (entity == null) {
            return null;
        }

        try {

            List<String> pinned =
                    objectMapper.readValue(entity.getPinnedItemHrefs(), new TypeReference<>() {});

            List<String> sided =
                    objectMapper.readValue(entity.getSidebarItemHrefs(), new TypeReference<>() {});

            Map<String, Boolean> nested =
                    objectMapper.readValue(entity.getOpenNestedItems(), new TypeReference<>() {});

            return UserSidebarConfigDto.builder()
                    .userId(entity.getUserId())
                    .pinnedItemHrefs(pinned)
                    .sidebarItemHrefs(sided)
                    .openNestedItems(nested)
                    .isCollapsed(entity.getIsCollapsed())
                    .isPartnerView(entity.getIsPartnerView())
                    .isVendorView(entity.getIsVendorView())
                    .build();

        } catch (Exception e) {
            return null;
        }
    }
}
