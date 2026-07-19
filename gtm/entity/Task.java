package com.sharkdom.gtm.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.gtm.common.ProgressStage;
import com.sharkdom.gtm.common.Status;
import com.sharkdom.gtm.common.TargetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "t_tasks")
public class Task extends BaseEntity {

    @JsonProperty("task_title")
    @Column(nullable = false, length = 100)
    private String title;

    @JsonProperty("status")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    @JsonProperty("stage")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProgressStage stage;

    @JsonProperty("target_type")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TargetType targetType;

    @JsonProperty("start_date")
    @Column(nullable = true)
    private Instant startDate;

    @JsonProperty("end_date")
    @Column(nullable = true)
    private Instant endDate;

    @JsonProperty("owner_id")
    private String ownerId;

    @JsonProperty("note")
    @Column(columnDefinition = "TEXT")
    private String note;

    @JsonProperty("external_partner_code")
    @Column(columnDefinition = "TEXT")
    private String externalPartnerCode;

    @JsonProperty
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @JsonProperty
    @Column(name = "external_partner_id", nullable = false)
    private Long externalPartnerId;

    @JsonProperty("username")
    @Transient
    private String username;

    @JsonManagedReference
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TaskComment> comments = new ArrayList<>();
}