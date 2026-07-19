package com.sharkdom.partnertraining.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "t_quiz_questions")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "quiz")
@EqualsAndHashCode(exclude = "quiz")
public class QuizQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private StageQuiz quiz;

    @Column(columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String options; // JSON

    private String correctAnswer;

    private Integer questionOrder;
}
