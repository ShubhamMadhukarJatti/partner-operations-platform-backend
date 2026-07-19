package com.sharkdom.repository.email;

import com.sharkdom.entity.email.EmailForwardStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailForwardStatsRepository extends JpaRepository<EmailForwardStats, Long> {
    Optional<EmailForwardStats> findTopBySenderEmailOrderByCreatedAtDesc(String originalSenderEmail);
}
