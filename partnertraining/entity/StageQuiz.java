package com.sharkdom.partnertraining.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "t_stage_quiz")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"stage", "questions"})
@EqualsAndHashCode(exclude = {"stage", "questions"})
public class StageQuiz extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "stage_id", nullable = false, unique = true)
    @JsonIgnore
    private CourseStage stage;

    private String title;

    @OneToMany(
            mappedBy = "quiz",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<QuizQuestion> questions = new HashSet<>();
}