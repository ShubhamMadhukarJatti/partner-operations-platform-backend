package com.sharkdom.partnertraining.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.partnertraining.enums.ContentType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "t_stage_content")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"stage", "images"})
@EqualsAndHashCode(exclude = {"stage", "images"})
public class StageContent extends BaseEntity {

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "stage_id", nullable = false)
    private CourseStage stage;

    @Column(columnDefinition = "TEXT")
    private String chapterTitle;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    // thumbnail URL (for video / document preview)
    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    // Google Drive / Cloud Drive Link (video or document)
    @Column(name = "drive_link", columnDefinition = "TEXT")
    private String driveLink;

    // Document link (PDF, DOCX, etc.)
    @Column(name = "document_link", columnDefinition = "TEXT")
    private String documentLink;

    @OneToMany(
            mappedBy = "stageContent",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<StageContentImage> images = new HashSet<>();
}