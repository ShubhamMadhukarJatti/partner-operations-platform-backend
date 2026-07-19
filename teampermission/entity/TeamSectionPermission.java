package com.sharkdom.teampermission.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "t_team_section_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamSectionPermission extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;
    // Example: PROPERTY_CREATE, USER_DELETE, ROLE_ASSIGN

    private String description;

    private String module;
    // Example: PROPERTY, USER, PAYMENT, ROLE
}
