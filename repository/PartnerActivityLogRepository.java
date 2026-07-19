package com.sharkdom.partnerattribution.repository;

import com.sharkdom.partnerattribution.entities.PartnerActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartnerActivityLogRepository extends JpaRepository<PartnerActivityLog, Long> {

    List<PartnerActivityLog> findByOrgIdAndPartnerOrgIdAndDealIdAndIsDeletedFalseOrderByActivityDateDesc(
            Long orgId, Long partnerOrgId,String dealId);
}