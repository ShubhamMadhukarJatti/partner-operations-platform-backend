package com.sharkdom.partnerprogram.repository;

import com.sharkdom.partnerprogram.entities.PartnerLead;
import com.sharkdom.partnerprogram.enums.LeadStatus;
import com.sharkdom.reseller.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PartnerLeadRepository extends JpaRepository<PartnerLead, Long> {

    Page<PartnerLead> findByUserId(String userId, Pageable pageable);

    /**
     * Fetch recent leads for a partner, sorted by submittedDate descending.
     * Leads without a submittedDate fall to the bottom (NULLS LAST).
     */
    @Query("""
            SELECT p FROM PartnerLead p
            WHERE p.userId = :userId
            ORDER BY
                CASE WHEN p.submittedDate IS NULL THEN 1 ELSE 0 END ASC,
                p.submittedDate DESC
            """)
    Page<PartnerLead> findRecentLeadsByUserId(
            @Param("userId") String userId,
            Pageable pageable
    );

    Long countByUserId(String userId);


    long countByUserIdAndLeadStatusIn(
            String userId,
            List<LeadStatus> statuses
    );

    @Query("""
            SELECT COALESCE(SUM(p.estimatedCommission), 0)
            FROM PartnerLead p
            WHERE p.userId = :userId
            AND p.paymentStatus = :paymentStatus
            """)
    BigDecimal sumCommissionByUserIdAndPaymentStatus(
            @Param("userId") String userId,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

}