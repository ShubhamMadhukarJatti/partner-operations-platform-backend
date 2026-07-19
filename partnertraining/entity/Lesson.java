package com.sharkdom.partnertraining.entity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.partnertraining.enums.LessonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name="t_lesson")
@NoArgsConstructor
@AllArgsConstructor
public class Lesson extends BaseEntity {

    private String title;

    @Enumerated(EnumType.STRING)
    private LessonType type;

    private String contentUrl;

    private Integer durationMinutes;

    private Integer sequenceOrder;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private Module module;
}
