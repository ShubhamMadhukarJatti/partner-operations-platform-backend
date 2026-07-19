package com.sharkdom.partnertraining.entity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.entity.organization.Organization;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name="t_course_enrollment")
@NoArgsConstructor
@AllArgsConstructor
public class CourseEnrollment extends BaseEntity {

    private Boolean completed = false;

    private Integer progressPercentage;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Organization user;
}
