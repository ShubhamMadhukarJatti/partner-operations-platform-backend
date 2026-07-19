package com.sharkdom.zoho.config;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "zoho_webhook_configs")
@Getter
@Setter
public class ZohoWebhookConfig extends BaseEntity {

    private Long tenantId;

    private String moduleName;

    private String tenantToken;
}