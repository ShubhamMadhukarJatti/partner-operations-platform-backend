package com.sharkdom.subscription.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_organization_suite")
@Data
public class OrganizationSuite extends BaseEntity {

    private Long organizationId;

    @Enumerated(EnumType.STRING)
    private SuiteKey suiteKey;

    private boolean active;
}