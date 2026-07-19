package com.sharkdom.zoho.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "crm_accounts")
@Getter
@Setter
public class ZohoCrmAccount extends BaseEntity {

    private Long tenantId;

    private String zohoRecordId;

    private String accountName;

    private String website;

    private String phone;

    private String industry;
}