package com.sharkdom.tablecustomization.controller.externalpartner;

import com.sharkdom.tablecustomization.service.externalpartner.ExternalPartnerColumnMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/external-partner/columns")
@RequiredArgsConstructor
@Slf4j
public class ExternalPartnerColumnController {

    private final ExternalPartnerColumnMigrationService migrationService;

    // =========================
    // SEED DEFAULT COLUMNS
    // =========================
    @PostMapping("/seed")
    public ResponseEntity<String> seedDefaultColumns() {

        log.info("EP_COLUMN_SEED_API_CALLED");

        migrationService.seedDefaultColumnsForAllTables();

        return ResponseEntity.ok("Default columns seeded successfully for all external partner tables");
    }
}
