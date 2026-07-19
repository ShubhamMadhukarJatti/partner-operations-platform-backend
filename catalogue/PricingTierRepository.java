package com.sharkdom.repository.catalogue;

import com.sharkdom.entity.catalogue.PricingTier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingTierRepository
        extends JpaRepository<PricingTier, Long> {

    Page<PricingTier> findByOrgId(Long orgId, Pageable pageable);
}

