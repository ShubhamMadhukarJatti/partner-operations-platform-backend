package com.sharkdom.repository.ppi;

import com.sharkdom.entity.ppi.PartnerPortalBranding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerPortalBrandingRepository extends JpaRepository<PartnerPortalBranding,Long> {

    Optional<PartnerPortalBranding> findByOrganizationId(Long organizationId);

    boolean existsByOrganizationId(Long organizationId);
}
