package com.sharkdom.zoho.controller;

import com.sharkdom.zoho.service.ZohoDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController

@RequestMapping("/zoho")
public class ZohoController {
    private final ZohoDocumentService zohoDocumentService;

    public ZohoController(ZohoDocumentService zohoDocumentService) {
        this.zohoDocumentService = zohoDocumentService;
    }


    @Operation(summary = "Sign Document using zoho")
    @PostMapping(value = "/sign-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadDocumentForSign(@RequestParam Long organizationCollaborationId, @RequestParam MultipartFile file) {
        zohoDocumentService.signDocument(organizationCollaborationId, file);
        return Map.of("message", "Document sign request sent successfully");
    }

    @PostMapping("sign-callback")
    public void signCallback(@RequestBody Map<Object, Object> request) {
        zohoDocumentService.handleCallback(request);
    }
}
