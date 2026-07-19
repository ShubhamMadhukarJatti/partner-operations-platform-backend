package com.sharkdom.tablecustomization.controller.externalpartner;

import com.sharkdom.tablecustomization.service.externalpartner.ExternalPartnerRowMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/external-partner/table")
@RequiredArgsConstructor
public class ExternalPartnerTableController {

    private final ExternalPartnerRowMigrationService syncService;

    /**
     * Sync OfflinePartnerInvite into External Partner Dynamic Table
     * Creates or Updates Row & Column Values
     */
    @PostMapping("/sync")
    public ResponseEntity<String> syncExternalPartnerTable(
    ) {
        syncService.migrateOfflinePartnersToDynamicTable();
        return ResponseEntity.ok("External Partner Table synced successfully.");
    }
}