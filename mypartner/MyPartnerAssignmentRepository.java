package com.sharkdom.repository.mypartner;

import com.sharkdom.entity.mypartner.MyPartnerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MyPartnerAssignmentRepository extends JpaRepository<MyPartnerAssignment,Long> {

    Optional<MyPartnerAssignment> findByPartnerOrgIdAndOrganizationId(Long partnerOrgId, Long organizationId);

    Optional<MyPartnerAssignment> findByOrganizationIdAndPartnerOrgId(Long organizationId,Long partnerOrganizationId);
}
