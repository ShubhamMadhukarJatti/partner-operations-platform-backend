package com.sharkdom.entity.organization;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "organization_follower",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"organizationId", "followerOrganizationId"})},
        indexes = {@Index(columnList = "organizationId", name = "organizationId_index"),
                @Index(columnList = "followerOrganizationId", name = "followerOrganizationId_index")})
public class OrganizationFollower extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    private long organizationId;
    private long followerOrganizationId;
    private String followingFor;
    private String followedByUserId;

}
