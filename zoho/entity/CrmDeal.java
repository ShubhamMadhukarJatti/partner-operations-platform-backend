package com.sharkdom.zoho.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "crm_deals")
@Getter
@Setter
public class CrmDeal extends BaseEntity {

    private Long tenantId;

    private String zohoRecordId;

    private String dealName;

    private String stage;

    private BigDecimal amount;

    private String accountName;
}
