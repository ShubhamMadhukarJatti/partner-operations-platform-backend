package com.sharkdom.zoho.service;

import com.sharkdom.zoho.config.ZohoWebhookConfig;
import com.sharkdom.zoho.repository.ZohoWebhookConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ZohoWebhookConfigService {

    private final ZohoWebhookConfigRepository repository;

    public ZohoWebhookConfig save(

            Long tenantId,

            String moduleName,

            String tenantToken

    ) {

        ZohoWebhookConfig config =
                new ZohoWebhookConfig();

        config.setTenantId(tenantId);

        config.setModuleName(moduleName);

        config.setTenantToken(tenantToken);

        return repository.save(config);
    }
}