package com.sharkdom.entity.organization;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "organization_follower_history")
public class OrganizationFollowerHistory extends OrganizationFollower {
    @Serial
    private static final long serialVersionUID = 1L;

    private long organizationId;
    private long followerOrganizationId;
    private String followStoppedByUserId;
    private Date followedSince;

    public OrganizationFollowerHistory(OrganizationFollower organizationFollower, String followStoppedByUserId) {
        super(organizationFollower.getOrganizationId(), organizationFollower.getFollowerOrganizationId(),
                organizationFollower.getFollowingFor(), organizationFollower.getFollowedByUserId());
        this.followStoppedByUserId = followStoppedByUserId;
        this.followedSince = organizationFollower.getCreationTimestamp();
    }
}
