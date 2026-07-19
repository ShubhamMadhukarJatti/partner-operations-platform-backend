package com.sharkdom.agenticai.repository;

import com.sharkdom.agenticai.entity.AiPromptHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiPromptHistoryRepository
        extends JpaRepository<AiPromptHistory, Long> {

    List<AiPromptHistory> findByOrgId(Long orgId);

}