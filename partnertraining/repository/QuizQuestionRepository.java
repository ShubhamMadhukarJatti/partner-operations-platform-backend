package com.sharkdom.partnertraining.repository;

import com.sharkdom.partnertraining.entity.QuizQuestion;
import com.sharkdom.partnertraining.entity.StageQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    Optional<QuizQuestion> findByQuiz(StageQuiz quiz);
}
