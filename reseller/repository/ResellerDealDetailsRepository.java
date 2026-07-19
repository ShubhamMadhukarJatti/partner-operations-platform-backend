package com.sharkdom.reseller.repository;

import com.sharkdom.constants.partnerDeals.DealStatus;
import com.sharkdom.entity.partenearDeals.Deal;
import com.sharkdom.reseller.entity.ResellerDealDetails;
import com.sharkdom.reseller.entity.ResellerDealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResellerDealDetailsRepository extends JpaRepository<ResellerDealDetails,Long> {

    @Query("SELECT r.resellerDealStatus, COUNT(r) FROM ResellerDealDetails r " +
            "WHERE (r.resellerOrgId = :orgId OR r.vendorOrgId = :orgId) " +
            "GROUP BY r.resellerDealStatus")
    List<Object[]> getResellerDealCountsByStatusForOrg(@Param("orgId") Long orgId);

    List<ResellerDealDetails> findByVendorOrgIdAndResellerDealStatus(long l, ResellerDealStatus resellerDealStatus);

    List<ResellerDealDetails> findByResellerOrgIdAndResellerDealStatus(long l, ResellerDealStatus resellerDealStatus);

    List<ResellerDealDetails> findByResellerOrgId(Long orgIdFromToken);

    List<ResellerDealDetails> findByVendorOrgId(Long orgIdFromToken);

    Optional<ResellerDealDetails> findByHubspotDealId(String hotspotDealId);


}
