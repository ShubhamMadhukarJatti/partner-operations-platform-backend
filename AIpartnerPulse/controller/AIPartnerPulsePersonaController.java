package com.sharkdom.AIpartnerPulse.controller;

import com.sharkdom.AIpartnerPulse.dto.PartnerPersonaFilteredResponse;
import com.sharkdom.AIpartnerPulse.dto.PartnerPersonaResponse;
import com.sharkdom.AIpartnerPulse.service.AIPartnerPulsePersonaService;
import com.sharkdom.AIpartnerPulse.service.EmailDomainService;
import com.sharkdom.AIpartnerPulse.service.OrganizationSearchService;
import com.sharkdom.entity.organization.OrganizationCustomResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/external/partner/pulse")
@RequiredArgsConstructor
public class AIPartnerPulsePersonaController {

    private final AIPartnerPulsePersonaService partnerPersonaService;
    private final OrganizationSearchService organizationSearchService;
    private final EmailDomainService emailDomainService;

    @PostMapping
    public PartnerPersonaResponse getPartnerPersona(@RequestBody String[] urls) {
        return partnerPersonaService.fetchPartnerPersonaData(urls);
    }

    @GetMapping("/filter/search")
    public ResponseEntity<?> searchByPersona(
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2000") int size) {

        long start = System.currentTimeMillis();
        log.info("[searchByPersona] Request received | email={} | page={} | size={}",
                email, page, size);

        try {
            String url = emailDomainService.getWebsiteFromEmail(email);
            log.info("[searchByPersona] Extracted website URL from email={} -> url={}", email, url);

            if (url == null || url.trim().isEmpty()) {
                log.warn("[searchByPersona] Invalid email={} | Unable to extract domain", email);
                return ResponseEntity.badRequest().body("Invalid email provided. Unable to extract domain.");
            }

            PartnerPersonaFilteredResponse personaResponse =
                    partnerPersonaService.fetchExternalPartnerPersonaData(url);

            if (personaResponse == null) {
                log.error("[searchByPersona] PartnerPersonaService returned NULL for url={}", url);
                return ResponseEntity.internalServerError()
                        .body("Partner Persona service returned null response.");
            }

            // Extract values
            String filters = personaResponse.getPredictedSubsectors();
            String sectorType = personaResponse.getRankedThresholdKeys();

            log.info("[searchByPersona] Persona Extracted | filters={} | sectorType={}",
                    filters, sectorType);

            if (sectorType != null) {
                sectorType = sectorType.trim().toUpperCase();
            }

            // If both empty → return empty
            if ((filters == null || filters.isBlank()) &&
                    (sectorType == null || sectorType.isBlank())) {

                log.info("[searchByPersona] No filters + no sectorType returned for url={} | returning empty", url);
                return ResponseEntity.ok(Page.empty());
            }

            // Convert CSV → list
            List<String> filtersList = (filters != null && !filters.isBlank())
                    ? Arrays.stream(filters.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList()
                    : Collections.emptyList();

            List<String> sectorTypeList = (sectorType != null && !sectorType.isBlank())
                    ? List.of(sectorType.trim())
                    : Collections.emptyList();

            log.info("[searchByPersona] Prepared Search Params | filtersList={} | sectorTypeList={}",
                    filtersList, sectorTypeList);

            Page<OrganizationCustomResponse> result =
                    organizationSearchService.searchByFilter(
                            filtersList,
                            Collections.emptyList(),
                            Collections.emptyList(),
                            null,
                            null,
                            page,
                            size
                    );

            long end = System.currentTimeMillis();
            log.info("[searchByPersona] Completed Successfully | resultCount={} | timeTaken={}ms",
                    result.getTotalElements(), (end - start));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            long end = System.currentTimeMillis();
            log.error("[searchByPersona] ERROR processing email={} | timeTaken={}ms",
                    email, (end - start), e);

            return ResponseEntity.internalServerError()
                    .body("Failed to process searchByPersona request");
        }
    }






}
