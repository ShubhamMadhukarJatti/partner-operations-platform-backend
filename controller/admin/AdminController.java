package com.sharkdom.controller.admin;

import com.sharkdom.entity.ai.Order;
import com.sharkdom.model.ai.ModeSaveResponse;
import com.sharkdom.service.ai.PersonaService;
import com.sharkdom.service.integration.PartnershipIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController

@RequestMapping
public class AdminController {
    private final PartnershipIntegrationService partnershipIntegrationService;
    private final PersonaService personaService;

    public AdminController(PartnershipIntegrationService partnershipIntegrationService, PersonaService personaService) {
        this.partnershipIntegrationService = partnershipIntegrationService;
        this.personaService = personaService;
    }

    @Operation(summary = "GET Partner Listing Mode Details")
    @GetMapping(path = "/partner-listing/mode-details")
    public Page<ModeSaveResponse> getListingDetails(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Order order) {
        return partnershipIntegrationService.getEntities(page, size, order);
    }

    @Operation(summary = "GET Persona Listing Mode Details")
    @GetMapping(path = "/persona/mode-details")
    public Page<ModeSaveResponse> getDetails(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Order order) {
        return personaService.getEntities(page, size, order);
    }

}
