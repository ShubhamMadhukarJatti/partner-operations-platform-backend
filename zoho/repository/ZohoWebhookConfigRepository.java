package com.sharkdom.zoho.repository;

import com.sharkdom.zoho.config.ZohoWebhookConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ZohoWebhookConfigRepository
        extends JpaRepository<ZohoWebhookConfig, Long> {

    Optional<ZohoWebhookConfig>
    findByTenantToken(String tenantToken);


}