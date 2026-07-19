package com.sharkdom.profilesection.repository;

import com.sharkdom.profilesection.entity.UserSidebarConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSidebarConfigRepository
        extends JpaRepository<UserSidebarConfig, Long> {

    Optional<UserSidebarConfig> findByUserId(String userId);
    Optional<UserSidebarConfig> findTopByUserIdAndIsPartnerViewTrueOrderByIdDesc(String userId);

    Optional<UserSidebarConfig> findTopByUserIdAndIsVendorViewTrueOrderByIdDesc(String userId);
}