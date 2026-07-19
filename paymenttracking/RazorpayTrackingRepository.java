package com.sharkdom.repository.paymenttracking;

import com.sharkdom.entity.paymenttracking.RazorpayTrackingEntity;
import com.sharkdom.model.paymenttracking.RazorpayPaymentDetailsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RazorpayTrackingRepository extends JpaRepository<RazorpayTrackingEntity, Long> {

    Optional<RazorpayTrackingEntity> findByPaymentId(String paymentId);

    @Query("SELECT new com.sharkdom.model.paymenttracking.RazorpayPaymentDetailsDto(r.affiliateCode, r.accountId, r.eventType, r.paymentId, r.orderId, r.amount, r.currency, r.status, r.method, r.bank, r.contact, r.email) " +
            "FROM RazorpayTrackingEntity r WHERE r.affiliateCode = :affiliateCode")
    List<RazorpayPaymentDetailsDto> findAllByReferralCode(@Param("affiliateCode") String affiliateCode);

    List<RazorpayTrackingEntity> findByOrganizationIdAndStatus(Long organizationId, String status);
}
