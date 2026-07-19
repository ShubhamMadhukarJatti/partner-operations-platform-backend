package com.sharkdom.repository.organization;

import com.sharkdom.entity.organization.OrganizationFollower;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationFollowerRepository extends JpaRepository<OrganizationFollower, Long> {

    Page<OrganizationFollower> findAllByFollowerOrganizationId(long followerOrganizationId, Pageable pageable);

    Page<OrganizationFollower> findAllByOrganizationId(long organizationId, Pageable pageable);

    Optional<OrganizationFollower> findByOrganizationIdAndFollowerOrganizationId(long organizationId, long followerOrganizationId);

    boolean existsByOrganizationIdAndFollowerOrganizationIdOrFollowerOrganizationIdAndOrganizationId(Long orgId1, Long orgId2, Long orgId3, Long orgId4);


    long deleteByOrganizationIdAndFollowerOrganizationId(long organizationId, long followerOrganizationId);
}
