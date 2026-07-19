package com.sharkdom.partnertraining.entity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.entity.organization.Organization;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@Table(name="t_partner_readiness")
@NoArgsConstructor
@AllArgsConstructor
public class PartnerReadiness extends BaseEntity {

    private Integer readinessScore;

    private Integer totalUsers;
    private Integer coursesEnrolled;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

}
