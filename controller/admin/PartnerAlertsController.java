package com.sharkdom.controller.admin;

import com.sharkdom.constants.Days;
import com.sharkdom.entity.admin.PartnerAlertsEntity;
import com.sharkdom.service.admin.PartnersAlertService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/partner-alert")
public class PartnerAlertsController {
    private final PartnersAlertService partnersAlertService;

    public PartnerAlertsController(PartnersAlertService partnersAlertService) {
        this.partnersAlertService = partnersAlertService;
    }

    @Operation(summary = "Disable/Enable Partner Alerts")
    @PostMapping("")
    public ResponseEntity<PartnerAlertsEntity> addOrganization(@RequestParam Days days, @RequestParam boolean disable) {
        return ResponseEntity.ok(partnersAlertService.disableAlert(days, disable));
    }

    @Operation(summary = "Get all Alerts status")
    @GetMapping("")
    public ResponseEntity<List<PartnerAlertsEntity>> getAllAlerts() {
        return ResponseEntity.ok(partnersAlertService.getAllAlerts());
    }

}
