package com.sharkdom.entity.ai;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.model.organizatiocollaboration.CollaborationCategory;
import com.sharkdom.model.ai.RecordType;
import jakarta.persistence.*;
import lombok.*;
import java.util.Map;

@Entity
@Table(name = "partner_data_permissions",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"organizationId", "collaborationCategory"})})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerDataPermissionEntity extends BaseEntity {
    private Long organizationId;
    
    @Enumerated(EnumType.STRING)
    private CollaborationCategory collaborationCategory;
    
    @OneToMany(mappedBy = "partnerDataPermission", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyEnumerated(EnumType.STRING)
    @MapKey(name = "recordType")
    private Map<RecordType, PartnerDataPermissionDetails> permissions;
} 