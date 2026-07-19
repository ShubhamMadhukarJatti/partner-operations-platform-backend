package com.sharkdom.mypartner.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.gtm.common.ProgressStage;
import com.sharkdom.gtm.common.Status;
import com.sharkdom.gtm.common.TargetType;
import com.sharkdom.gtm.entity.TaskComment;
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
@Table(name = "t_my_partner_tasks")
public class MyPartnerTask extends BaseEntity {

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

    @JsonProperty
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @JsonProperty
    @Column(name = "my_partner_id", nullable = false)
    private Long myPartnerId;


    @JsonProperty("user_name")
    private String userName;

    @JsonManagedReference
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MyPartnerTaskComment> comments = new ArrayList<>();
}