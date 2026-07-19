package com.sharkdom.zoho.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "zoho_tenants")
public class ZohoTenant extends BaseEntity {

    private String companyName;

    private String email;

    private String organizationId;

    private Boolean active;
}