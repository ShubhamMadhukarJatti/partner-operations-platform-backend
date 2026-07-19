package com.sharkdom.partnertraining.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "t_course_certificates",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"course_id", "user_id", "org_id"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCertificate extends BaseEntity {

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "certificate_url", columnDefinition = "TEXT", nullable = false)
    private String certificateUrl;
}