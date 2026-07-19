package com.sharkdom.deals.repository;

import com.sharkdom.constants.user.ApprovalRequestHistoryStatus;
import com.sharkdom.deals.entity.DealsJoinerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealsJoinerRepository extends JpaRepository<DealsJoinerEntity, Long> {
    List<DealsJoinerEntity> findAllByDealId(String dealId);

    List<DealsJoinerEntity> findAllByOrganizationId(Long organizationId);

    DealsJoinerEntity findByOrganizationIdAndDealId(Long organizationId, String dealId);

    List<DealsJoinerEntity> findAllByDealIdAndStatus(String dealId, ApprovalRequestHistoryStatus status);
}
