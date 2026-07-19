package com.sharkdom.repository.integration;

import com.sharkdom.entity.integration.PartnershipIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnershipIntegrationRepository extends JpaRepository<PartnershipIntegration, Long> {
    PartnershipIntegration findByOrganizationId(Long organizationId);

    //boolean existsByOrganizationId(Long organizationId);
}
