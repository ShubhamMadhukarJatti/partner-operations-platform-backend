package com.sharkdom.partnertraining.entity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.partnertraining.enums.UserCourseStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "mypartner_user_course_status",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_course_assigned",
                        columnNames = {"course_id", "assigned_org_id"}
                )
        }
)
@Data
public class MyPartnerUserCourseStatusEntity extends BaseEntity {

    // jisne course assign kiya
    @Column(name = "assigning_org_id", nullable = false)
    private Long assigningOrgId;

    // jisko course assign hua
    @Column(name = "assigned_org_id", nullable = false)
    private Long assignedOrgId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserCourseStatus status;
}
