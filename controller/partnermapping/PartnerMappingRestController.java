package com.sharkdom.controller.partnermapping;

import com.sharkdom.entity.partnermapping.MyPartnerMappingReportStatus;
import com.sharkdom.service.partnermapping.PartnerMappingService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/partner-mapping")
public class PartnerMappingRestController {

    @Autowired
    private PartnerMappingService partnerMappingService;

    @GetMapping("/overview")
    public ResponseEntity<String> getPartnerMappingDetails() {
        try {
            JSONObject response = partnerMappingService.getPartnerMappingDetails();
            return ResponseEntity.ok(response.toString());
        } catch (Exception ex) {
            // Proper error logging
            log.error("Error while fetching partner mapping details", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to fetch partner mapping details\"}");
        }
    }

    @GetMapping("/comparison")
    public ResponseEntity<String> getPartnerMappingComparison(@RequestParam String typeCombination) {
        try {
            JSONObject response = partnerMappingService.getPartnerMappingReportDetails(typeCombination);
            return ResponseEntity.ok(response.toString());
        } catch (Exception ex) {
            // Proper error logging
            log.error("Error while fetching partner mapping details", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to fetch partner mapping details\"}");
        }
    }

    @GetMapping("/eps/comparison")
    public ResponseEntity<String> getPartnerMappingEpsComparison(@RequestParam String typeCombination) {
        try {
            JSONObject response = partnerMappingService.getPartnerMappingReportDetails(typeCombination);
            return ResponseEntity.ok(response.toString());
        } catch (Exception ex) {
            // Proper error logging
            log.error("Error while fetching partner mapping details", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to fetch partner mapping details\"}");
        }
    }

    @PostMapping("/report/save")
    public ResponseEntity<String> savePartnerMappingReport(@RequestBody MyPartnerMappingReportStatus reportStatus) {
        try {
            JSONObject response = partnerMappingService.savePartnerMappingReport(reportStatus);
            return ResponseEntity.ok(response.toString());
        } catch (Exception ex) {
            // Proper error logging
            log.error("Error while saving partner mapping report", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to save partner mapping report\"}");
        }
    }

    @GetMapping("/report/history")
    public ResponseEntity<String> getPartnerMappingReportStatus() {
        try {
            JSONObject response = partnerMappingService.getPartnerMappingReportStatus();
            return ResponseEntity.ok(response.toString());
        } catch (Exception ex) {
            // Proper error logging
            log.error("Error while fetching partner mapping report status", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to fetch partner mapping report status\"}");
        }
    }
}
