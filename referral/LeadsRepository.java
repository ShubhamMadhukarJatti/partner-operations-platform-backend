package com.sharkdom.repository.referral;

import com.sharkdom.entity.referral.LeadsEntity;
import com.sharkdom.model.referral.LeadsInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LeadsRepository extends JpaRepository<LeadsEntity, Long> {
    @Query(value = """
            SELECT
                DATE(creationTimestamp) AS date,
                name AS name,
                email AS email,
                leadsStatus AS leadsStatus
            FROM
               LeadsEntity
            WHERE
                referralCode = :referralCode
                AND DATE(creationTimestamp) BETWEEN :from AND :to
            """)
    List<LeadsInfo> findAllByReferralCode(String referralCode, LocalDate from, LocalDate to);

    @Query(value = """
            SELECT count(*)
            FROM
               LeadsEntity
            WHERE
                referralCode = :referralCode
                
            """)
    int count(String referralCode);

    Optional<LeadsEntity> findByEmailAndReferralCode(String email, String referralCode);

    Page<LeadsEntity> findAllByReferralCodeOrderByCreationTimestampDesc(String referralCode, Pageable pageable);

    int countByReferralCode(String referralCode);

    @Query("SELECT COUNT(l) FROM LeadsEntity l WHERE l.referralCode = :referralCode AND DATE(l.creationTimestamp) BETWEEN :fromDate AND :toDate")
    long countLeadsByReferralCode(@Param("referralCode") String referralCode,
                                  @Param("fromDate") LocalDate fromDate,
                                  @Param("toDate") LocalDate toDate);
}