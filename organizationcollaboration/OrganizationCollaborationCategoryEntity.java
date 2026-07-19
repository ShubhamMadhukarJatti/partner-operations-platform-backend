package com.sharkdom.entity.organizationcollaboration;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.model.organizatiocollaboration.CollaborationCategory;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "organization_collaboration_category")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class OrganizationCollaborationCategoryEntity extends BaseEntity {
    private Long organizationId;
    private Long organizationCollaborationId;
    private CollaborationCategory category;
}
