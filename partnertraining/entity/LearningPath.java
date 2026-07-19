package com.sharkdom.partnertraining.entity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.partnertraining.enums.CourseLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name="t_learning_path")
@NoArgsConstructor
@AllArgsConstructor
public class LearningPath extends BaseEntity {

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String coverImageUrl;

    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    private CourseLevel level;

    @ManyToMany
    @JoinTable(
            name = "learning_path_courses",
            joinColumns = @JoinColumn(name = "learning_path_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses = new ArrayList<>();
}
