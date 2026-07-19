package com.sharkdom.repository.organization;

import com.sharkdom.entity.organization.ShortlistOrganization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShortlistOrganizationRepository extends JpaRepository<ShortlistOrganization,Long> {

    Page<ShortlistOrganization> findByShortlistedByOrgId(Long orgId, Pageable pageable);

    Optional<ShortlistOrganization> findByShortlistedByOrgIdAndShortlistedOrgId(Long orgId1, Long orgId2);

    void deleteByShortlistedByOrgIdAndShortlistedOrgId(Long orgId1, Long orgId2);


}
