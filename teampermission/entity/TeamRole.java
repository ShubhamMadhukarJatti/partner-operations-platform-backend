package com.sharkdom.teampermission.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "team_section_roles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"org_id", "name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRole extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "role_permission_codes",
            joinColumns = @JoinColumn(name = "role_id")
    )
    @Column(name = "permission_code")
    private Set<String> permissionCodes = new HashSet<>();
}
