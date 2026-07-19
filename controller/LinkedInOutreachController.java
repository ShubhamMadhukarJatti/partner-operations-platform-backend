package com.sharkdom.agenticai.controller;

import com.sharkdom.agenticai.model.GenerateLinkedinNoteRequest;
import com.sharkdom.agenticai.model.SendConnectionRequestRequest;
import com.sharkdom.agenticai.service.LinkedInOutreachService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/linkedin/outreach")
@RequiredArgsConstructor
@Tag(name = "linkedin_outreach")
public class LinkedInOutreachController {

    private final LinkedInOutreachService outreachService;

    @Operation(summary = "Get Account Status")
    @GetMapping("/accounts/{account_id}/status")
    public ResponseEntity<Map<String, Object>> status(@PathVariable("account_id") String accountId) {
        return ResponseEntity.ok(outreachService.getAccountStatus(accountId));
    }

    @Operation(summary = "List Connections")
    @GetMapping("/accounts/{account_id}/connections")
    public ResponseEntity<Map<String, Object>> connections(@PathVariable("account_id") String accountId) {
        return ResponseEntity.ok(outreachService.listConnections(accountId));
    }

    @Operation(summary = "Generate Linkedin Connection Note")
    @PostMapping("/generate-note")
    public ResponseEntity<Map<String, Object>> generateNote(
            @RequestBody GenerateLinkedinNoteRequest req) {
        return ResponseEntity.ok(outreachService.generateNote(req));
    }

    @Operation(summary = "Send Connection Request")
    @PostMapping("/send-connection-request")
    public ResponseEntity<Map<String, Object>> sendRequest(
            @RequestBody SendConnectionRequestRequest req) {
        return ResponseEntity.ok(outreachService.sendConnectionRequest(req));
    }
}