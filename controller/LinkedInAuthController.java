package com.sharkdom.agenticai.controller;

import com.sharkdom.agenticai.model.LinkedInCookieConnectRequest;
import com.sharkdom.agenticai.service.LinkedinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "linkedin")
public class LinkedInAuthController {

    private final LinkedinService linkedInService;

    @Operation(summary = "List Accounts")
    @GetMapping("/accounts")
    public ResponseEntity<Map<String, Object>> listAccounts() {
        return ResponseEntity.ok(linkedInService.listAccounts());
    }

    @Operation(summary = "Build Linkedin Connection")
    @GetMapping("/authenticate")
    public ResponseEntity<Map<String, Object>> authenticate() {
        return ResponseEntity.ok(linkedInService.authenticate());
    }

    @Operation(summary = "Create Reconnect Link")
    @GetMapping("/accounts/reconnect")
    public ResponseEntity<Map<String, Object>> reconnect() {
        return ResponseEntity.ok(linkedInService.reconnect());
    }

    @Operation(summary = "Connect Linkedin Cookie")
    @PostMapping("/authenticate/cookie")
    public ResponseEntity<Map<String, Object>> cookie(
            @RequestBody LinkedInCookieConnectRequest req) {
        return ResponseEntity.ok(linkedInService.authenticateCookie(req));
    }

    @Operation(summary = "Delete Account")
    @DeleteMapping("/accounts/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestParam String accountId) {
        return ResponseEntity.ok(linkedInService.deleteAccount(accountId));
    }
}