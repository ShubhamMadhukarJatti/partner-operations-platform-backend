package com.sharkdom.partnerattribution.service;

import com.sharkdom.partnerattribution.dto.OutreachResponse;
import com.sharkdom.partnerattribution.dto.PartnerIntroductionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorToPartnerOutreachService {

    private final RestTemplate restTemplate;

    private static final String API_URL =
            "https://outreach-engine-dhhvhdewhbfbe3a7.eastus-01.azurewebsites.net/outreach/vendor-to-partner";

    private static final String API_KEY = "test-key-1";

    public OutreachResponse generateVendorToPartnerIntro(PartnerIntroductionDTO request) {

        log.info("Starting Vendor -> Partner outreach generation");
        log.info("Sender: {} from {}", request.getSenderName(), request.getSenderCompany());
        log.info("Target Company: {}", request.getTargetAccountName());

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", API_KEY);

            HttpEntity<PartnerIntroductionDTO> entity = new HttpEntity<>(request, headers);

            log.info("Calling Outreach Engine API");

            ResponseEntity<OutreachResponse> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    entity,
                    OutreachResponse.class
            );

            OutreachResponse body = response.getBody();

            log.info("Outreach API response received successfully");
            log.info("Generated subject: {}", body != null ? body.getSubject() : null);
            log.info("Word count: {}", body != null ? body.getWordCount() : null);

            return body;

        } catch (Exception ex) {

            log.error("Error while calling Outreach Engine API");
            log.error("Sender: {}", request.getSenderName());
            log.error("Target: {}", request.getTargetAccountName());
            log.error("Exception: ", ex);

            throw new RuntimeException("Failed to generate outreach message", ex);
        }
    }

    public OutreachResponse generatePartnerToTargetIntro(PartnerIntroductionDTO request) {

        String apiUrl =
                "https://outreach-engine-dhhvhdewhbfbe3a7.eastus-01.azurewebsites.net/outreach/partner-to-target";

        log.info("Starting Partner -> Target outreach generation");
        log.info("Partner: {} from {}", request.getPartnerContactName(), request.getPartnerCompany());
        log.info("Target Company: {}", request.getTargetAccountName());

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", "test-key-1");

            HttpEntity<PartnerIntroductionDTO> entity = new HttpEntity<>(request, headers);

            log.info("Calling Partner-To-Target Outreach API");

            ResponseEntity<OutreachResponse> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    OutreachResponse.class
            );

            OutreachResponse body = response.getBody();

            log.info("Partner -> Target outreach generated successfully");
            log.info("Recipient: {}", body != null ? body.getRecipientName() : null);
            log.info("Generated subject: {}", body != null ? body.getSubject() : null);
            log.info("Word count: {}", body != null ? body.getWordCount() : null);

            return body;

        } catch (Exception ex) {

            log.error("Error while calling Partner-To-Target Outreach API");
            log.error("Partner: {}", request.getPartnerContactName());
            log.error("Target: {}", request.getTargetAccountName());
            log.error("Exception occurred: ", ex);

            throw new RuntimeException("Failed to generate partner-to-target outreach", ex);
        }
    }

    public OutreachResponse generateOutreach(PartnerIntroductionDTO request) {

        if (request.getSenderType() == null) {
            throw new IllegalArgumentException("SenderType is required");
        }

        switch (request.getSenderType()) {

            case VENDOR_TO_PARTNER:
                return generateVendorToPartnerIntro(request);

            case PARTNER_TO_TARGET:
                return generatePartnerToTargetIntro(request);

            default:
                throw new IllegalArgumentException("Invalid SenderType");
        }
    }
}