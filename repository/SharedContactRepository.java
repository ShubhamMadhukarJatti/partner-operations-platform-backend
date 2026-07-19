package com.sharkdom.partnerattribution.repository;

import com.sharkdom.partnerattribution.entities.SharedContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedContactRepository extends JpaRepository<SharedContact, Long> {

    List<SharedContact> findByOrgIdAndPartnerOrgIdAndIsDeletedFalse(
            Long orgId, Long partnerOrgId);

    List<SharedContact> findByOrgIdAndPartnerOrgIdAndDealIdAndIsDeletedFalse(
            Long orgId, Long partnerOrgId, String dealId);
}