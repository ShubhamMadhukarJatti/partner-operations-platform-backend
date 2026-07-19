package com.sharkdom.repository.externalpartner;

import com.sharkdom.entity.externalpartner.ExternalPartnerAssignment;
import com.sharkdom.entity.mypartner.MyPartnerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExternalPartnerAssignmentRepository extends JpaRepository<ExternalPartnerAssignment,Long> {

    Optional<ExternalPartnerAssignment> findByOrganizationIdAndExternalPartnerId(Long organizationId, Long partnerOrganizationId);
}
