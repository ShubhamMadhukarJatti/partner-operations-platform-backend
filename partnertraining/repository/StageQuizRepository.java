package com.sharkdom.partnertraining.repository;

import com.sharkdom.partnertraining.entity.StageQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StageQuizRepository extends JpaRepository<StageQuiz,Long> {
    Optional<StageQuiz> findByStageId(Long stageId);
    boolean existsByStageId(Long stageId);
}
