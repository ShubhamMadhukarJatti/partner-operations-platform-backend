package com.sharkdom.profilesection.repository;

import com.sharkdom.profilesection.entity.OrganizationCertification;
import com.sharkdom.profilesection.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrganizationCertificationRepository extends JpaRepository<OrganizationCertification, Long> {

    List<OrganizationCertification> findByOrganizationId(Long organizationId);
    List<OrganizationCertification> findByOrganizationIdAndStatus(Long organizationId,VerificationStatus status);
    Page<OrganizationCertification> findByStatus(VerificationStatus status, Pageable pageable);

    @Query("""
       SELECT oc.certificationName
       FROM OrganizationCertification oc
       WHERE oc.organizationId = :orgId
       AND oc.status = com.sharkdom.profilesection.enums.VerificationStatus.VERIFIED
       """)
    List<String> findVerifiedCertificationNames(Long orgId);

}