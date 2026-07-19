package com.sharkdom.profilesection.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_organization_search_counter",
        uniqueConstraints = @UniqueConstraint(columnNames = "organization_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationSearchCounter extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "search_count", nullable = false)
    private Integer searchCount;
}