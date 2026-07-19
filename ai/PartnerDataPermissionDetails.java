package com.sharkdom.entity.ai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.model.ai.PartnerDataPermission.AccessType;
import com.sharkdom.model.ai.RecordType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "partner_data_permission_details")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerDataPermissionDetails extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "permission_id")
    @JsonIgnore
    private PartnerDataPermissionEntity partnerDataPermission;

    @Enumerated(EnumType.STRING)
    private RecordType recordType;

    @Enumerated(EnumType.STRING)
    private AccessType accessType;
    
    @ElementCollection
    @CollectionTable(name = "partner_data_shared_fields",
                    joinColumns = @JoinColumn(name = "permission_details_id"))
    @Column(name = "field_name")
    private Set<String> sharedFields;
} 