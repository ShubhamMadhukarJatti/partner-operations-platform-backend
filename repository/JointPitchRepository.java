package com.sharkdom.partnerattribution.repository;

import com.sharkdom.partnerattribution.entities.JointPitch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JointPitchRepository extends JpaRepository<JointPitch, Long> {

    Optional<JointPitch> findByOrgIdAndPartnerOrgIdAndDealIdAndIsDeletedFalse(
            Long orgId, Long partnerOrgId,String dealId);
}