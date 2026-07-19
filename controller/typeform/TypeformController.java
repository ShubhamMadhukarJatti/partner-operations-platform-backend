package com.sharkdom.controller.typeform;

import com.sharkdom.model.typeform.TypeFormRefreshResponse;
import com.sharkdom.model.typeform.TypeFormTokenResponse;
import com.sharkdom.model.typeform.TypeformRequest;
import com.sharkdom.model.typeform.TypeformResponse;
import com.sharkdom.service.typeform.TypeformService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/typeForm")
@Slf4j
public class TypeformController {

    @Autowired
    private TypeformService typeformService;

    @GetMapping("/authorize")
    public ResponseEntity<Map<String, String>> authorize() {
        return ResponseEntity.ok(typeformService.getAuthorizationUrl());
    }

    @Operation(summary = "Typeform callback endpoint", security = {})
    @GetMapping("/callback")
    @PermitAll
    public ResponseEntity<TypeFormTokenResponse> callback(@RequestParam("code") String code) throws JSONException {
        return ResponseEntity.ok(typeformService.handleAuthorization(code));
    }

    @PostMapping("/create-form")
    public ResponseEntity<TypeformResponse> createForm(@RequestParam(value = "organizationId") Long organizationId,
                                                       @RequestParam(value = "collaborationId") Long collaborationId,
                                                       @RequestHeader(value = "accessToken", required = false) String accessToken,
                                                       @RequestBody TypeformRequest request) throws JSONException {
        return ResponseEntity.ok(typeformService.createTypeform(organizationId, accessToken, request));
    }

//    @PostMapping("/add-webhook")
//    public ResponseEntity<TypeFormWebhookResponse> addWebhook(@RequestParam("formId") String formId, @RequestParam("webhookUrl") String webhookUrl, @RequestParam("tag") String tag) throws JSONException {
//        return ResponseEntity.ok(typeformService.addWebhook(formId, webhookUrl, tag));
//    }

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveWebhook(@RequestBody String payload) throws JSONException {
        return ResponseEntity.ok(typeformService.receiveWebhook(payload));
    }
        // Basic validation


    @GetMapping("/retrieve-webhook")
    public ResponseEntity<TypeformResponse> retrieveWebhook(@RequestParam("formId") String formId) throws JSONException {
        return ResponseEntity.ok(typeformService.retrieveWebhook(formId));
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<TypeFormRefreshResponse> refreshToken() throws Exception {
        return ResponseEntity.ok(typeformService.refreshAccessToken());
    }

}
