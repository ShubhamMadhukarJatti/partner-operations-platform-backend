package com.sharkdom.teampermission.entity;


import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "t_org_team_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "organization_id"}))
@AllArgsConstructor
@NoArgsConstructor
public class TeamMembers extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @ManyToMany
    @JoinTable(
            name = "team_member_roles",
            joinColumns = @JoinColumn(name = "team_member_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<TeamUserRoles> roles = new HashSet<>();


}
