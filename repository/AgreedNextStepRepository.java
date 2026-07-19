package com.sharkdom.partnerattribution.repository;

import com.sharkdom.partnerattribution.entities.AgreedNextStep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgreedNextStepRepository extends JpaRepository<AgreedNextStep, Long> {

    Page<AgreedNextStep> findByOrgIdAndDealIdAndIsDeletedFalse(Long orgId, String dealId,Pageable pageable);

}