package com.sharkdom.repository.ppi;

import com.sharkdom.entity.ppi.PartnerProgramDNSData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartnerProgramDNSDataRepository extends JpaRepository<PartnerProgramDNSData,Long> {
    boolean existsByCustomDomain(String customDomain);

    Optional<PartnerProgramDNSData> findByOrganizationId(Long orgId);

    Optional<PartnerProgramDNSData> findByCustomDomain(String customDomain);

    Optional<PartnerProgramDNSData> findByTargetHostIgnoreCase(String targetHost);

    Optional<PartnerProgramDNSData> findByAzureDomainResourceName(String azureDomainResourceName);

    List<PartnerProgramDNSData> findByAzureDomainResourceNameIsNotNull();
}
