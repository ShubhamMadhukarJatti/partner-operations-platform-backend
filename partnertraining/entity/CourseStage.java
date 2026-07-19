package com.sharkdom.partnertraining.entity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.partnertraining.enums.StageType;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "t_course_stage")
@NoArgsConstructor
@AllArgsConstructor
public class CourseStage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private String title;

    @Enumerated(EnumType.STRING)
    private StageType type;   // CONTENT / QUIZ

    private Integer stageOrder;

    private Boolean active = true;
}
