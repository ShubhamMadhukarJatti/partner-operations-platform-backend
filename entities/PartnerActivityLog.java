package com.sharkdom.partnerattribution.entities;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_partner_activity_log")
@Data
public class PartnerActivityLog extends BaseEntity {

    private Long orgId;

    private Long partnerOrgId;

    private String dealId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String activityType;
    private String actor;

    private LocalDateTime activityDate;

    @Column(columnDefinition = "json")
    private String metadata;

    private Boolean isDeleted = false;
}