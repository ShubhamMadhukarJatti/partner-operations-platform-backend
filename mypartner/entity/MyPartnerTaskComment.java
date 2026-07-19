package com.sharkdom.mypartner.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.gtm.entity.Task;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_my_partner_task_comments")
public class MyPartnerTaskComment extends BaseEntity {

    @JsonProperty("comment_text")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String commentText;

    @JsonProperty("commented_by")
    @Column(nullable = false)
    private String commentedBy;

    @JsonProperty("commented_at")
    @Column(name = "commented_at", nullable = false)
    private Instant commentedAt;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private MyPartnerTask task;

    @PreUpdate
    public void onUpdate() {
        this.commentedAt = Instant.now();
    }
}
