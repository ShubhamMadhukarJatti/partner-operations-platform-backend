package com.sharkdom.partnerattribution.entities;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.partnerattribution.enums.Priority;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "t_agreed_next_steps")
@Data
public class AgreedNextStep extends BaseEntity {

    private Long orgId;

    private Long partnerOrgId;

    private String dealId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String owner;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private LocalDate dueDate;

    private Boolean isCompleted = false;

    private Boolean isDeleted = false;

}
