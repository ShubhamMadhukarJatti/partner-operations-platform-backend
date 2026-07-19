package com.sharkdom.agenticai.repository;

import com.sharkdom.agenticai.entity.OutreachAutomationSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OutreachAutomationRepository
        extends JpaRepository<OutreachAutomationSettings, Long> {

    Optional<OutreachAutomationSettings> findByOrgId(Long orgId);

}

