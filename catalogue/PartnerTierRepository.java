package com.sharkdom.repository.catalogue;

import com.sharkdom.entity.catalogue.PartnerTier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerTierRepository extends JpaRepository<PartnerTier,Long> {
    Page<PartnerTier> findByOrgId(Long orgId, Pageable pageable);

    Optional<PartnerTier> findTopByOrgIdAndSeatLowerLessThanEqualAndSeatUpperGreaterThanEqualAndActiveTrue(
            Long orgId, Integer lower, Integer upper);

    boolean existsByOrgId(Long orgId);

    boolean existsByOrgIdAndActiveTrue(Long orgId);

}
