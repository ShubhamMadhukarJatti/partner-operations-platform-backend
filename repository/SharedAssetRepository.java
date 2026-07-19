package com.sharkdom.partnerattribution.repository;

import com.sharkdom.partnerattribution.entities.SharedAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedAssetRepository extends JpaRepository<SharedAsset, Long> {

    List<SharedAsset> findByOrgIdAndPartnerOrgIdAndDealIdAndIsDeletedFalse(
            Long orgId, Long partnerOrgId,String dealId);
}