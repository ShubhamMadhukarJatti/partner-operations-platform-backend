package com.sharkdom.zoho.controller;

import com.sharkdom.zoho.config.ZohoWebhookConfig;
import com.sharkdom.zoho.dto.ZohoWebhookConfigRequest;
import com.sharkdom.zoho.service.ZohoWebhookConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/zoho/webhook-intehration")
@RequiredArgsConstructor
public class ZohoWebhookIntegrationController {

    private final ZohoWebhookConfigService service;

    @PostMapping
    public ResponseEntity<?> save(

            @RequestBody
            ZohoWebhookConfigRequest request

    ) {

        ZohoWebhookConfig response =
                service.save(

                        request.getTenantId(),

                        request.getModuleName(),

                        request.getTenantToken()
                );

        return ResponseEntity.ok(response);
    }
}
