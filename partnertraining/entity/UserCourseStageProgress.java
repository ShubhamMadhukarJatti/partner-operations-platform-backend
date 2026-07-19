package com.sharkdom.partnertraining.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_course_stage_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "course_id", "stage_id"}
                )
        }
)
@Data
public class UserCourseStageProgress extends BaseEntity {

    private String userId;

    private Long courseId;

    private Long stageId;

    private boolean completed;

    private LocalDateTime completedAt;

    private Long orgId;
}

