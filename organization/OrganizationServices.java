package com.sharkdom.entity.organization;

import com.sharkdom.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization_services")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class OrganizationServices extends BaseEntity {

    String service;

    public OrganizationServices(String service) {
        this.service = service;
    }
}
