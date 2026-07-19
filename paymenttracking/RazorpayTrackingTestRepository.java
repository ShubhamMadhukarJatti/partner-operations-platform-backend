package com.sharkdom.repository.paymenttracking;

import com.sharkdom.entity.paymenttracking.RazorpayTrackingTestEntity;
import com.sharkdom.model.paymenttracking.RazorpayPaymentDetailsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RazorpayTrackingTestRepository extends JpaRepository<RazorpayTrackingTestEntity, Long> {
    @Query("SELECT new com.sharkdom.model.paymenttracking.RazorpayPaymentDetailsDto(r.affiliateCode, r.accountId, r.eventType, r.paymentId, r.orderId, r.amount, r.currency, r.status, r.method, r.bank, r.contact, r.email) " +
            "FROM RazorpayTrackingTestEntity r WHERE r.affiliateCode = :affiliateCode")
    List<RazorpayPaymentDetailsDto> findAllByReferralCode(@Param("affiliateCode") String affiliateCode);
}
