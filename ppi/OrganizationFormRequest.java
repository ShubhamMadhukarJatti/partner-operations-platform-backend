package com.sharkdom.entity.ppi;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "t_org_form_request",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sender_receiver_org",
                        columnNames = {"sender_org_id", "receiver_org_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationFormRequest extends BaseEntity {

    @Column(name = "sender_org_id", nullable = false)
    private Long senderOrgId;

    @Column(name = "receiver_org_id", nullable = false)
    private Long receiverOrgId;

    @Column(name = "form_id")
    private String formId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FormStatus status;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "denied_at")
    private LocalDateTime deniedAt;
}
