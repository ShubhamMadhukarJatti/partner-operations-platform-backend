package com.sharkdom.partnerattribution.addtopipeline;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerDealRepository extends JpaRepository<PartnerDeal, Long> {

    Optional<PartnerDeal> findByDealId(String dealId);

    boolean existsByDealId(String dealId);
}
