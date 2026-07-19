package com.sharkdom.service.ppi;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.dto.DnsConnectionStatusResponse;
import com.sharkdom.dto.PartnerProgramDNSCreateRequest;
import com.sharkdom.dto.PartnerProgramDNSVerifyRequest;
import com.sharkdom.entity.organization.OrganizationUserMapping;
import com.sharkdom.entity.ppi.PartnerProgramDNSData;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.model.ppi.DnsInstruction;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.ppi.PartnerProgramDNSDataRepository;
import com.sharkdom.util.Util;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.naming.Context;
import javax.naming.directory.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class DnsUtil {

    @Autowired
    private PartnerProgramDNSDataRepository partnerProgramDNSDataRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AzureCdnService azureCdnService;

    @Value("${azure.afd.endpoint}")
    private String afdEndpoint;

    @Value("${azure.afd.route-name}")
    private String routeName;

    @Value("${azure.afd.origin-group}")
    private String originGroup;

    @Value("${env}")
    private String env;

    @Autowired
    private TaskScheduler taskScheduler;

    public static boolean checkCname(String domain, String expectedTarget) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

            DirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(domain, new String[]{"CNAME"});

            Attribute cname = attrs.get("CNAME");
            if (cname == null) {
                return false;
            }

            for (int i = 0; i < cname.size(); i++) {
                String target = cname.get(i)
                        .toString()
                        .replace(".", "")
                        .toLowerCase();

                if (target.equals(
                        expectedTarget.replace(".", "").toLowerCase()
                )) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Transactional
    public PartnerProgramDNSData createDNSData(
            PartnerProgramDNSCreateRequest request
    ) {
        var orgId = Util.getOrgIdFromToken();
        var optOrg = organizationRepository.findById(orgId);
        // Optional validation
        if (partnerProgramDNSDataRepository.existsByCustomDomain(request.getCustomDomain())) {
            throw new IllegalStateException(
                    "Custom domain already exists: " + request.getCustomDomain()
            );
        }
        PartnerProgramDNSData dnsData = new PartnerProgramDNSData();
        dnsData.setCustomDomain(request.getCustomDomain());
        dnsData.setTargetHost(request.getTargetHost());
        dnsData.setOrganizationId(orgId);
        dnsData.setFormUrl(optOrg.get().getResponderUrl());
        return partnerProgramDNSDataRepository.save(dnsData);
    }

    @Transactional
    public PartnerProgramDNSData verifyAndSaveDns(
            PartnerProgramDNSVerifyRequest request
    ) {
        try {
            Long orgId = Util.getOrgIdFromToken();
            if (orgId == null) {
                throw new ServiceException(ErrorMessages.SH21);
            }

            // Validate request
            if (request.getCustomDomain() == null || request.getCustomDomain().isBlank()) {
                throw new ServiceException(ErrorMessages.SH165);
            }

            if (request.getTargetHost() == null || request.getTargetHost().isBlank()) {
                throw new ServiceException(ErrorMessages.SH172);
            }

            boolean isVerified = checkCname(
                    request.getCustomDomain(),
                    request.getTargetHost()
            );

            if (!isVerified) {
                throw new ServiceException(
                        ErrorMessages.SH169,
                        request.getCustomDomain()
                );
            }
            PartnerProgramDNSData dnsData =
                    partnerProgramDNSDataRepository
                            .findByOrganizationId(orgId)
                            .orElseGet(PartnerProgramDNSData::new);
            dnsData.setOrganizationId(orgId);
            dnsData.setCustomDomain(request.getCustomDomain());
            dnsData.setTargetHost(request.getTargetHost());
            dnsData.setRecordType(request.getRecordType());
            dnsData.setValue(request.getValue());
            dnsData.setFormId(request.getFormId());
            String baseUrl = "PROD".equalsIgnoreCase(env)
                    ? "https://www.sharkdom.com/form-viewer.html"
                    : "https://dev.sharkdom.com/form-viewer.html";
            dnsData.setFormUrl(baseUrl);
            dnsData.setSubdomainName(request.getSubdomainName());
            dnsData.setDomain(request.getDomain());
            // ---- Azure CDN Integration ----
            var updateCustomDomain = azureCdnService.createOrUpdateCustomDomain(request.getCustomDomain(), request.getCustomDomain());
            if (updateCustomDomain == null) {
                throw new ServiceException(ErrorMessages.SH170, request.getCustomDomain());
            }
            dnsData.setAzureDomainResourceName(updateCustomDomain.getName());
            markAllRecordsAsAssociated();
            return partnerProgramDNSDataRepository.save(dnsData);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException(
                    ErrorMessages.SH116,
                    ex.getMessage()
            );
        }
    }

    @Transactional
    public void markAllRecordsAsAssociated() {
        List<PartnerProgramDNSData> list = partnerProgramDNSDataRepository.findAll();
        list.forEach(record -> record.setAssociated(false));
        partnerProgramDNSDataRepository.saveAll(list);
    }

    @Transactional
    public void markAllRecordsAsAssociatedAndMakeItTrue(String azureCustomDomain) {
        var optAzureCustomDomain = partnerProgramDNSDataRepository.findByAzureDomainResourceName(azureCustomDomain);
        if (optAzureCustomDomain.isPresent())
        {
            var partnerProgramDNSData = optAzureCustomDomain.get();
            partnerProgramDNSData.setAssociated(true);
            partnerProgramDNSDataRepository.save(partnerProgramDNSData);
        }
    }

    public DnsInstruction generateDnsValidationRecord(
            String customDomain,
            String validationToken
    ) {
        log.info("Generating DNS validation record for domain: {}", customDomain);
        // Azure requires TXT record for CDN domain validation
        String type = "TXT";
        String name = "_dnsauth." + customDomain;
        String value = validationToken;

        return new DnsInstruction(type, name, value);
    }

    public boolean checkDnsConnectionStatus() {
        Long orgId = Util.getOrgIdFromToken();
        // Step 1: Check if DNS data exists for this org
        var optDnsData =
                partnerProgramDNSDataRepository.findByOrganizationId(orgId);
        if (optDnsData.isEmpty()) {
            // No DNS data mapped with this org
            return false;
        }
        PartnerProgramDNSData dnsData = optDnsData.get();
        // Step 2: Verify CNAME using existing org DNS data
        boolean isVerified = checkCname(
                dnsData.getCustomDomain(),
                dnsData.getTargetHost()
        );
        return isVerified;
    }

    public String getFormUrlByCustomDomain(String customDomain) {

        log.info("Fetching formUrl for customDomain={}", customDomain);

        if (!StringUtils.hasText(customDomain)) {
            log.error("Invalid customDomain received");
            throw new ServiceException(ErrorMessages.SH165);
        }

        return partnerProgramDNSDataRepository.findByCustomDomain(customDomain)
                .map(PartnerProgramDNSData::getFormUrl)
                .orElseThrow(() -> {
                    log.error("No form found for customDomain={}", customDomain);
                    return new ServiceException(
                            ErrorMessages.SH164,
                            customDomain
                    );
                });
    }


    public String getFormIdByCustomDomain(String customDomain) {

        log.info("Fetching formId for customDomain={}", customDomain);

        if (!StringUtils.hasText(customDomain)) {
            log.error("Invalid customDomain received");
            throw new ServiceException(ErrorMessages.SH165);
        }

        return partnerProgramDNSDataRepository.findByCustomDomain(customDomain)
                .map(PartnerProgramDNSData::getFormId)
                .orElseThrow(() -> {
                    log.error("No formId found for customDomain={}", customDomain);
                    return new ServiceException(
                            ErrorMessages.SH164,
                            customDomain
                    );
                });
    }

    public PartnerProgramDNSData getDnsDataByOrganizationId(Long organizationId) {

        log.info("Fetching DNS data for organizationId: {}", organizationId);

        return partnerProgramDNSDataRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> {
                    log.warn("DNS data not found for organizationId: {}", organizationId);
                    return new RuntimeException("DNS data not found for organization");
                });
    }

    public Map<String, String> getDomainValidationStatusByOrgId() {
        Long organizationId = Util.getOrgIdFromToken();

        long startTime = System.currentTimeMillis();
        log.info("Fetching domain validation status for organizationId={}", organizationId);

        try {
            // Step 1: Find DNS data by ORG ID
            PartnerProgramDNSData dnsData = partnerProgramDNSDataRepository
                    .findByOrganizationId(organizationId)
                    .orElseThrow(() -> {
                        log.warn("No DNS data found for organizationId={}", organizationId);
                        return new ServiceException(
                                ErrorMessages.SH164,
                                organizationId
                        );
                    });

            String azureDomainResourceName = dnsData.getAzureDomainResourceName();

            if (azureDomainResourceName == null || azureDomainResourceName.isBlank()) {
                log.warn("Azure domain resource name missing for organizationId={}", organizationId);
                throw new ServiceException(
                        ErrorMessages.SH165
                );
            }

            log.info("Found Azure domain resource name={} for organizationId={}",
                    azureDomainResourceName, organizationId);

            // Step 2: Call Azure
            String domainValidationState =
                    azureCdnService.getCustomDomainValidationState(azureDomainResourceName);

            if (domainValidationState == null ) {
                log.error("Invalid Azure response for domain={}", azureDomainResourceName);
                throw new ServiceException(
                        ErrorMessages.SH171,
                        azureDomainResourceName
                );
            }

            log.info("Azure domain validation state={} for domain={}",
                    domainValidationState, azureDomainResourceName);

            // Step 3: Return as Key-Value Pair
            return Map.of(
                    "domainValidationState", domainValidationState != null
                            ? domainValidationState
                            : "UNKNOWN"
            );

        } catch (ServiceException se) {
            log.error("Service exception while fetching domain status for orgId={}: {}",
                    organizationId, se.getMessage());
            throw se;
        } catch (Exception ex) {
            log.error("Unexpected error while fetching domain status for orgId={}",
                    organizationId, ex);
            throw new ServiceException(
                    ErrorMessages.SH171,
                    organizationId
            );
        } finally {
            log.info("Domain validation check completed for organizationId={} in {} ms",
                    organizationId,
                    System.currentTimeMillis() - startTime);
        }
    }

    public DnsInstruction getTxtRecord(String customDomainName) {

        long startTime = System.currentTimeMillis();
        log.info("TXT record generation started for customDomain={}", customDomainName);

        try {
            // Step 1: Validate input
            if (!StringUtils.hasText(customDomainName)) {
                log.warn("Invalid customDomainName received");
                throw new ServiceException(ErrorMessages.SH165);
            }

            // Step 2: Call Azure to fetch validation token
            String validationToken =
                    azureCdnService.getCustomDomainValidationToken(AzureCdnService.convertDotToDash(customDomainName));

            log.info("Custom Domain Validation Token received for domain={}: {}",
                    customDomainName, validationToken);

            // Step 3: Handle pending/null state
            if (!StringUtils.hasText(validationToken)) {
                log.info("Domain validation token is still pending for domain={}", customDomainName);
                return null; // Controller will convert this to PENDING response
            }

            // Step 4: Generate DNS instruction
            DnsInstruction validationRecord =
                    generateDnsValidationRecord(customDomainName, validationToken);

            log.info("TXT record generated successfully for customDomain={} in {} ms",
                    customDomainName,
                    System.currentTimeMillis() - startTime);

            return validationRecord;

        } catch (ServiceException se) {
            log.error("Service exception while generating TXT record for domain={}: {}",
                    customDomainName, se.getMessage());
            throw se;
        } catch (Exception ex) {
            log.error("Unexpected error while generating TXT record for domain={}",
                    customDomainName, ex);
            throw new ServiceException(
                    ErrorMessages.SH171,
                    customDomainName
            );
        }
    }

    public DnsConnectionStatusResponse checkDnsConnectionStatusWithTxtValidation() {

        Long orgId = Util.getOrgIdFromToken();
        log.info("Checking DNS connection status (independent flags) for orgId={}", orgId);

        DnsConnectionStatusResponse response = new DnsConnectionStatusResponse();

        // Default values
        response.setDnsDataExists(false);
        response.setCnameVerified(false);
        response.setAzureValidated(false);

        // Step 1: Fetch DNS data
        var optDnsData = partnerProgramDNSDataRepository.findByOrganizationId(orgId);

        // Flag 1: DNS Data Exists
        if (optDnsData.isEmpty()) {
            log.warn("No DNS data found for orgId={}", orgId);
            return response; // no data, but still returning independent flags
        }

        response.setDnsDataExists(true);
        PartnerProgramDNSData dnsData = optDnsData.get();

        // Step 2: CNAME Verification (Independent)
        try {
            boolean isCnameVerified = checkCname(
                    dnsData.getCustomDomain(),
                    dnsData.getTargetHost()
            );
            response.setCnameVerified(isCnameVerified);
        } catch (Exception e) {
            log.error("Error while verifying CNAME for orgId={}", orgId, e);
            response.setCnameVerified(false);
        }

        // Step 3: Azure Validation (Independent)
        try {
            String azureDomainResourceName = dnsData.getAzureDomainResourceName();

            if (azureDomainResourceName != null && !azureDomainResourceName.isBlank()) {

                String domainValidationState =
                        azureCdnService.getCustomDomainValidationState(azureDomainResourceName);

                log.info("Azure domain validation state={} for orgId={}",
                        domainValidationState, orgId);

                boolean isAzureValidated =
                        domainValidationState != null &&
                                !domainValidationState.equalsIgnoreCase("Pending");

                response.setAzureValidated(isAzureValidated);
            } else {
                log.warn("Azure domain resource name missing for orgId={}", orgId);
                response.setAzureValidated(false);
            }

        } catch (Exception e) {
            log.error("Error while checking Azure validation for orgId={}", orgId, e);
            response.setAzureValidated(false);
        }
        return response;
    }

    public void updateAllRoutes(
            String afdEndpointName,
            String routeName,
            String originGroupName
    ) {

        List<PartnerProgramDNSData> dnsRecords =partnerProgramDNSDataRepository
                .findAll();

        if (dnsRecords.isEmpty()) {

            log.warn(
                    "No DNS records found"
            );

            return;
        }

        log.info(
                "Starting Azure route update for {} domains",
                dnsRecords.size()
        );

        dnsRecords.forEach(record ->
                updateSingleRouteAsync(
                        afdEndpointName,
                        routeName,
                        originGroupName,
                        record
                )
        );

    }

    @Async
    public CompletableFuture<Void> updateSingleRouteAsync(
            String afdEndpointName,
            String routeName,
            String originGroupName,
            PartnerProgramDNSData dnsData
    ) {

        String customDomainName =
                dnsData.getAzureDomainResourceName();

        try {

            retryUpdate(
                    afdEndpointName,
                    routeName,
                    originGroupName,
                    customDomainName,
                    3
            );

            log.info(
                    "SUCCESS: Route updated for domain={}",
                    customDomainName
            );

        } catch (Exception ex) {

            log.error(
                    "FAILED: Route update failed for domain={}",
                    customDomainName,
                    ex
            );

        }

        return CompletableFuture.completedFuture(null);
    }

    private void retryUpdate(
            String afdEndpointName,
            String routeName,
            String originGroupName,
            String customDomainName,
            int maxAttempts
    ) {

        int attempt = 0;

        while (attempt < maxAttempts) {

            try {

//                azureCdnService.updateRouteWithCustomDomainV3(
//                        afdEndpointName,
//                        routeName,
//                        originGroupName,
//                        customDomainName
//                );

                markAllRecordsAsAssociatedAndMakeItTrue(customDomainName);

                return;

            } catch (Exception ex) {

                attempt++;

                log.warn(
                        "Retry {}/{} for domain={}",
                        attempt,
                        maxAttempts,
                        customDomainName
                );

                if (attempt >= maxAttempts) {

                    throw ex;

                }

                sleep();
            }
        }
    }

    private void sleep() {

        try {

            Thread.sleep(2000);

        } catch (InterruptedException ignored) {
        }
    }

    public void scheduleCRON() {
        Instant runAt = Instant.now().plus(60, ChronoUnit.MINUTES);

        taskScheduler.schedule(() -> {
            updateAllRoutes(afdEndpoint, routeName, originGroup);
        }, runAt);
    }

    public List<PartnerProgramDNSData> getAllDnsData() {
        return partnerProgramDNSDataRepository.findAll();
    }



}