package com.sharkdom.zoho.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZohoWebhookConfigRequest {

    private Long tenantId;

    private String moduleName;

    private String tenantToken;
}