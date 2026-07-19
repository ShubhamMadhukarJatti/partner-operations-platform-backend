package com.sharkdom.repository.referral;

import com.sharkdom.entity.referral.ImpressionEntity;
import com.sharkdom.model.referral.Impressions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ImpressionsRepository extends JpaRepository<ImpressionEntity, Long> {

    @Query(value = "SELECT \n" +
            "    DATE(creationTimestamp) AS date,\n" +
            "    COUNT(DISTINCT ipAddress) AS count\n" +
            "FROM \n" +
            "   ImpressionEntity \n" +
            "WHERE \n" +
            "    referralCode = :referralCode\n" +
            "    AND DATE(creationTimestamp) BETWEEN :from AND :to\n" +
            "GROUP BY \n" +
            "    DATE(creationTimestamp)\n")
    List<Impressions> distinctCountByReferralCode(String referralCode, LocalDate from, LocalDate to);

    @Query(value = "SELECT \n" +
            "    DATE(creationTimestamp) AS date,\n" +
            "    COUNT(ipAddress) AS count\n" +
            "FROM \n" +
            "   ImpressionEntity \n" +
            "WHERE \n" +
            "    referralCode = :referralCode\n" +
            "    AND DATE(creationTimestamp) BETWEEN :from AND :to\n" +
            "GROUP BY \n" +
            "    DATE(creationTimestamp)\n")
    List<Impressions> countImpressionsByReferralCode(String referralCode, LocalDate from, LocalDate to);

    @Query(value = """
            SELECT count(*)
            FROM
               ImpressionEntity
            WHERE
                referralCode = :referralCode
            """)
    int count(String referralCode);

    int countByReferralCode(String referralCode);
}