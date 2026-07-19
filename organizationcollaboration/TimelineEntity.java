package com.sharkdom.entity.organizationcollaboration;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "timeline")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TimelineEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;
    private Long organizationCollaborationId;
    private String action;
    private String template;
}
