package com.sharkdom.partnertraining.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "t_stage_content_images")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "stageContent")
@EqualsAndHashCode(exclude = "stageContent")
public class StageContentImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "stage_content_id", nullable = false)
    private StageContent stageContent;

    @Column(nullable = false)
    private String imageUrl;

    private Integer imageOrder;
}