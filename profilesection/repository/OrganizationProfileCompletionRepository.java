package com.sharkdom.profilesection.repository;

import com.sharkdom.profilesection.dto.ProfileCompletionType;
import com.sharkdom.profilesection.entity.OrganizationProfileCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationProfileCompletionRepository
        extends JpaRepository<OrganizationProfileCompletion, Long> {

    // Get all completion records for an org
    List<OrganizationProfileCompletion> findByOrganizationId(Long organizationId);

    // Get specific type for an org
    Optional<OrganizationProfileCompletion> findByOrganizationIdAndType(
            Long organizationId,
            ProfileCompletionType type
    );

    // Check if a specific step is completed
    boolean existsByOrganizationIdAndTypeAndCompletedTrue(
            Long organizationId,
            ProfileCompletionType type
    );

    // Delete all records for an org (optional use case)
    void deleteByOrganizationId(Long organizationId);
}