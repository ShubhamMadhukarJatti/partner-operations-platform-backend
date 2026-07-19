package com.sharkdom.partnertraining.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(
        name = "partner_portal_course_share",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_course_share",
                        columnNames = {"course_id", "sender_organization_id", "receiver_user_id"}
                )
        }
)
@Data
public class PartnerPortalCourseShare extends BaseEntity {

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "sender_organization_id", nullable = false)
    private Long senderOrganizationId;

    @Column(name = "receiver_user_id", nullable = false)
    private String receiverUserId;

    @Column(name = "receiver_user_email", nullable = false)
    private String receiverUserEmail;

    @Column(name = "shared_url", columnDefinition = "TEXT")
    private String sharedUrl;

    @Column(name = "is_active")
    private boolean active = true;
}
