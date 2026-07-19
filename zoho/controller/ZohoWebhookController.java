package com.sharkdom.zoho.controller;

import com.sharkdom.zoho.service.ZohoWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/zoho")
@RequiredArgsConstructor
@Slf4j
public class ZohoWebhookController {

    private final ZohoWebhookService webhookService;

    @PostMapping
    public ResponseEntity<?> webhook(

            @RequestBody Map<String, Object> payload

    ) {

        log.info("Webhook Payload: {}", payload);

        webhookService.process(payload);

        return ResponseEntity.ok().build();
    }

}