package com.sharkdom.partnerattribution.entities;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_joint_pitch",
        uniqueConstraints = @UniqueConstraint(columnNames = {"orgId", "partnerOrgId"}))
@Data
public class JointPitch extends BaseEntity {

    private Long orgId;

    private Long partnerOrgId;

    private String dealId;

    @Column(columnDefinition = "TEXT")
    private String pitch;

    private String lastEditedBy;
    private LocalDateTime lastEditedAt;

    private Boolean isDeleted = false;
}
