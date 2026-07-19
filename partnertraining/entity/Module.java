package com.sharkdom.partnertraining.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name="t_module")
@NoArgsConstructor
@AllArgsConstructor
public class Module extends BaseEntity {

    private String title;

    private Integer sequenceOrder;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}
