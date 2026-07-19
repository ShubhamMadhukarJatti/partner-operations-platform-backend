package com.sharkdom.repository.user;

import com.sharkdom.entity.user.SlackIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlackIntegrationRepository extends JpaRepository<SlackIntegration, Long> {
    Optional<SlackIntegration> findByUserId(String userId);

    void deleteByUserId(String userId);
}