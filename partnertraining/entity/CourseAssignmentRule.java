package com.sharkdom.partnertraining.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "t_course_assignment_rules")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseAssignmentRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(columnDefinition = "TEXT")
    private String tier;

    @Column(columnDefinition = "TEXT")
    private String geography;

    @Column(name = "program_type", columnDefinition = "TEXT")
    private String programType;
}