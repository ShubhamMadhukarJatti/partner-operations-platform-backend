package com.sharkdom.zoho.service;

import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.model.ai.ZohoToken;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.service.ai.ZohoService;
import com.sharkdom.zoho.client.ZohoApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZohoWebhookService {

    private final IntegrationRepository repository;

    private final ZohoApiClient zohoApiClient;

    private final ZohoAuthService zohoOAuthService;

    private final ZohoModuleRouter moduleRouter;

    private final ZohoService zohoService;

    public void process(
            Map<String, Object> payload
    ) {

        log.info("Zoho Webhook Processing Started");
        log.info("Incoming Payload : {}", payload);

        String tenantToken =
                String.valueOf(
                        payload.get("tenantToken")
                );

        String recordId =
                String.valueOf(
                        payload.get("recordId")
                );

        String module =
                String.valueOf(
                        payload.get("module")
                );

        log.info("Tenant Token : {}", tenantToken);
        log.info("Record Id : {}", recordId);
        log.info("Module : {}", module);

        log.info("Fetching integration details");

        IntegrationDetails integration =
                repository
                        .findByZohoTenantToken(
                                tenantToken
                        )
                        .orElseThrow(() -> {

                            log.error(
                                    "Integration not found for tenant token : {}",
                                    tenantToken
                            );

                            return new RuntimeException(
                                    "Integration not found"
                            );
                        });

        log.info(
                "Integration Found | OrgId={} | ApiDomain={}",
                integration.getOrganizationId(),
                integration.getZohoApiDomain()
        );

        log.info("Generating access token using refresh token");

        ZohoToken zohoToken =
                zohoService.generateRefreshToken(
                        integration.getRefreshToken(),
                        integration.getPublishableKey()
                );

        log.info(
                "Access Token Generated : {}",
                zohoToken != null
                        ? zohoToken.getAccess_token()
                        : "NULL"
        );

        log.info(
                "Calling Zoho API | Module={} | RecordId={}",
                module,
                recordId
        );

        Map response =
                zohoApiClient.getRecord(
                        integration.getZohoApiDomain(),
                        zohoToken.getAccess_token(),
                        module,
                        recordId
                );

        log.info(
                "Zoho Response : {}",
                response
        );

        List<Map<String, Object>> data =
                (List<Map<String, Object>>)
                        response.get("data");

        if (data == null || data.isEmpty()) {

            log.warn(
                    "No record found in Zoho response"
            );

            return;
        }

        Map<String, Object> record =
                data.get(0);

        log.info(
                "Record Retrieved : {}",
                record
        );

        log.info(
                "Routing Module Processing | OrgId={} | Module={}",
                integration.getOrganizationId(),
                module
        );

        moduleRouter.process(
                integration.getOrganizationId(),
                module,
                record
        );

        log.info(
                "Zoho Webhook Processing Completed Successfully"
        );
    }
}