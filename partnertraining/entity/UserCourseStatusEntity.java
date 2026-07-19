package com.sharkdom.partnertraining.entity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.partnertraining.enums.UserCourseStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "user_course_status",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_course",
                        columnNames = {"user_id", "course_id"}
                )
        }
)
@Data
public class UserCourseStatusEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserCourseStatus status;

    // jisne course assign kiya
    @Column(name = "assigning_org_id", nullable = false)
    private Long assigningOrgId;
}
