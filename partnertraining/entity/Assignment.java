package com.sharkdom.partnertraining.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name="t_assignment")
@NoArgsConstructor
@AllArgsConstructor
public class Assignment extends BaseEntity {

    private String title;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    private Integer maxScore;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}
