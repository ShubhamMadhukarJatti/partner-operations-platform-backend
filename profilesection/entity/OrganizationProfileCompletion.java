package com.sharkdom.profilesection.entity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.profilesection.dto.ProfileCompletionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_organization_profile_completion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationProfileCompletion extends BaseEntity {

    @Column(nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileCompletionType type;

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false)
    private int weight;
}