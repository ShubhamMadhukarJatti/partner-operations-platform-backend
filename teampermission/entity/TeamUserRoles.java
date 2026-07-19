package com.sharkdom.teampermission.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_team_members_user_roles",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "organization_id"}))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamUserRoles extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
}