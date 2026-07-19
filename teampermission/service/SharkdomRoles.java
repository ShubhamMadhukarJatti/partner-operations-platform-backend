package com.sharkdom.teampermission.service;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "t_sharkdom_roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_role_name", columnNames = "role_name")
        },
        indexes = {
                @Index(name = "idx_role_name", columnList = "role_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharkdomRoles extends BaseEntity {

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(name = "description", nullable = false, length = 500)
    private String description;
}