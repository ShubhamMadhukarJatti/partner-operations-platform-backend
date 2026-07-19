package com.sharkdom.entity.organization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "shortlist_organization")
public class ShortlistOrganization extends BaseEntity {

    // Organization that is shortlisted
    @JsonProperty("shortlisted_org_id")
    @Column(name = "shortlisted_org_id", nullable = false)
    private Long shortlistedOrgId;

    // User who shortlisted
    @JsonProperty("shortlisted_by_user_id")
    @Column(name = "shortlisted_by_user_id", nullable = false)
    private String shortlistedByUserId;

    // User name who shortlisted
    @JsonProperty("shortlisted_by_user_name")
    @Column(name = "shortlisted_by_user_name", nullable = false)
    private String shortlistedByUserName;

    // Organization of the user who shortlisted
    @JsonProperty("shortlisted_by_org_id")
    @Column(name = "shortlisted_by_org_id", nullable = false)
    private Long shortlistedByOrgId;

    // Remark of the user who shortlisted the organization
    @JsonProperty("remark")
    @Column(name = "remark")
    private String remark;

}
