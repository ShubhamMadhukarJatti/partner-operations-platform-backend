package com.sharkdom.partnertraining.entity;


import com.sharkdom.entity.BaseEntity;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.partnertraining.enums.CourseLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name="t_course")
@NoArgsConstructor
@AllArgsConstructor
public class Course extends BaseEntity {

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    private CourseLevel level;


    private Integer durationMinutes;

    private Boolean published = false;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Organization createdBy;

    @Column(columnDefinition = "TEXT")
    private String certificateUrl;

    @ManyToMany
    @JoinTable(
            name = "t_course_labels",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    private Set<Label> labels = new HashSet<>();
}
