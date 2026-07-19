package com.sharkdom.profilesection.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_org_seniority_counter")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationSeniorityCounter extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long organizationId;

    @Column(nullable = false)
    private Integer counter;
}