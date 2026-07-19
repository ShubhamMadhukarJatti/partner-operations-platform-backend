package com.sharkdom.repository.email;

import com.sharkdom.entity.email.EmailStatistics;
import com.sharkdom.model.email.AllCampaignStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface EmailStatisticsRepository extends JpaRepository<EmailStatistics, Long> {

    @Query("SELECT e FROM EmailStatistics e " +
            "WHERE (:eventType IS NULL OR e.eventType = :eventType) " +
            "AND (:env IS NULL OR e.env = :env) " +
            "AND (:templateCode IS NULL OR e.templateCode = :templateCode) " +
            "AND (:sentAt IS NULL OR e.sentAt = :sentAt) ")
    Page<EmailStatistics> getAllByEventTypeAndEnvAndTemplateCodeAndSentAt(String eventType, String env, String templateCode, LocalDate sentAt, Pageable pageable);

    boolean existsByEventTypeAndEnvAndEmailAndTemplateCode(String eventType, String env, String email, String templateCode);

    @Query("SELECT new com.sharkdom.model.email.AllCampaignStats(" +
            "e.templateCode, " +
            "e.sentAt, " +
            "SUM(CASE WHEN e.eventType = 'bounce' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN e.eventType = 'open' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN e.eventType = 'click' THEN 1 ELSE 0 END)) " +
            "FROM EmailStatistics e " +
            "GROUP BY e.templateCode, e.sentAt " +
            "ORDER BY e.sentAt DESC")
    Page<AllCampaignStats> getAllCampaignStats(Pageable pageable);

}
