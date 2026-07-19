package com.sharkdom.teampermission.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "t_team_user_roles_assignments",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "role_id", "organization_id"}
        ))
@Getter
@Setter
public class TeamUserRolesAssignment extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "organization_id")
    private Long organizationId;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private TeamUserRoles role;
}