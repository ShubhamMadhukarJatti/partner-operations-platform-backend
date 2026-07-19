package com.sharkdom.partnertraining.repository;

import com.sharkdom.partnertraining.entity.StageContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StageContentRepository extends JpaRepository<StageContent, Integer> {
    Optional<StageContent> findByStageId(Long stageId);
    boolean existsByStageId(Long stageId);
}
