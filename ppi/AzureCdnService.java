package com.sharkdom.service.ppi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.entity.ppi.AzureCustomDomainResponse;
import com.sharkdom.entity.ppi.PartnerProgramDNSData;
import com.sharkdom.model.ppi.*;
import com.sharkdom.repository.ppi.PartnerProgramDNSDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class AzureCdnService {

    private final RestTemplate restTemplate;
    private final AzureAuthService azureAuthService;
    private final PartnerProgramDNSDataRepository partnerProgramDNSDataRepository;

    @Value("${azure.subscription-id}")
    private String subscriptionId;

    @Value("${azure.resource-group}")
    private String resourceGroup;

    @Value("${azure.cdn-profile}")
    private String cdnProfile;

    @Value("${azure.api-version}")
    private String apiVersion;

    public AzureCdnService(RestTemplate restTemplate,
                           AzureAuthService azureAuthService, PartnerProgramDNSDataRepository partnerProgramDNSDataRepository) {
        this.restTemplate = restTemplate;
        this.azureAuthService = azureAuthService;
        this.partnerProgramDNSDataRepository = partnerProgramDNSDataRepository;
    }

    // ---------------- CREATE / UPDATE CUSTOM DOMAIN ----------------

    public AzureCustomDomainResponse createOrUpdateCustomDomain(
            String customDomainName,
            String hostName
    ) {
        try {
            String accessToken = azureAuthService.fetchAccessToken().getAccessToken();

            String url = String.format(
                    "https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Cdn/profiles/%s/customDomains/%s?api-version=%s",
                    subscriptionId,
                    resourceGroup,
                    cdnProfile,
                    convertDotToDash(customDomainName),
                    apiVersion
            );

            AzureCustomDomainRequest request = buildRequest(hostName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<AzureCustomDomainRequest> entity =
                    new HttpEntity<>(request, headers);

            log.info("Calling Azure CDN Custom Domain API");
            log.debug("Azure URL: {}", url);

            ResponseEntity<JsonNode> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.PUT,
                            entity,
                            JsonNode.class
                    );

            JsonNode body = response.getBody();

            if (body == null) {
                throw new RuntimeException("Empty response from Azure CDN");
            }

            String name = body.path("name").asText(null);

            String validationToken =
                    body.path("properties")
                            .path("validationProperties")
                            .path("validationToken")
                            .asText(null);

            if (name == null) {
                throw new RuntimeException("Azure response does not contain domain name");
            }

            return new AzureCustomDomainResponse(name, validationToken);

        } catch (Exception ex) {
            log.error("Unexpected error while creating CDN custom domain", ex);
            throw new RuntimeException("Azure CDN service error", ex);
        }
    }


    // ---------------- GET CUSTOM DOMAIN DETAILS ----------------

    public AzureCustomDomainStatusResponse getCustomDomainDetails(String customDomainName) {
        long startTime = System.currentTimeMillis();

        try {
            String accessToken = azureAuthService.fetchAccessToken().getAccessToken();

            String url = String.format(
                    "https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Cdn/profiles/%s/customDomains/%s?api-version=%s",
                    subscriptionId,
                    resourceGroup,
                    cdnProfile,
                    customDomainName,
                    apiVersion
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<AzureCustomDomainStatusResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            AzureCustomDomainStatusResponse.class
                    );

            log.info("CDN custom domain details fetched in {} ms",
                    System.currentTimeMillis() - startTime);

            return response.getBody();

        } catch (Exception ex) {
            log.error("Unexpected error while fetching CDN custom domain details", ex);
            throw new RuntimeException("Azure CDN service error", ex);
        }
    }

    // ---------------- GET VALIDATION STATE ----------------

    public String getCustomDomainValidationState(String customDomainName) {
        try {
            JsonNode response = fetchDomainJson(customDomainName);
            return response
                    .path("properties")
                    .path("domainValidationState")
                    .asText(null);
        } catch (Exception ex) {
            log.error("Unexpected error while fetching domainValidationState", ex);
            throw new RuntimeException("Azure CDN service error", ex);
        }
    }

    // ---------------- GET VALIDATION TOKEN ----------------

    public String getCustomDomainValidationToken(String customDomainName) {
        try {
            JsonNode response = fetchDomainJson(customDomainName);
            return response
                    .path("properties")
                    .path("validationProperties")
                    .path("validationToken")
                    .asText(null);
        } catch (Exception ex) {
            log.error("Unexpected error while fetching validationToken", ex);
            throw new RuntimeException("Azure CDN service error", ex);
        }
    }

    // ---------------- UPDATE ROUTE ----------------

    public void updateRouteWithCustomDomain(
            String afdEndpointName,
            String routeName,
            String originGroupName,
            String customDomainName
    ) {
        try {
            String accessToken = azureAuthService.fetchAccessToken().getAccessToken();

            String url = String.format(
                    "https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Cdn/profiles/%s/afdEndpoints/%s/routes/%s?api-version=%s",
                    subscriptionId,
                    resourceGroup,
                    cdnProfile,
                    afdEndpointName,
                    routeName,
                    apiVersion
            );

            AzureAfdRouteRequest request =
                    buildAfdRouteRequest(originGroupName, customDomainName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<AzureAfdRouteRequest> entity =
                    new HttpEntity<>(request, headers);

            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    Void.class
            );

            log.info("AFD route updated successfully");

        } catch (Exception ex) {
            log.error("Unexpected error while updating AFD route", ex);
            throw new RuntimeException("Azure AFD service error", ex);
        }
    }

    // ---------------- HELPERS ----------------

    private JsonNode fetchDomainJson(String customDomainName) {
        String accessToken = azureAuthService.fetchAccessToken().getAccessToken();

        String url = String.format(
                "https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Cdn/profiles/%s/customDomains/%s?api-version=%s",
                subscriptionId,
                resourceGroup,
                cdnProfile,
                customDomainName,
                apiVersion
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        JsonNode.class
                );

        return response.getBody();
    }

    public static String convertDotToDash(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.replace(".", "-");
    }

    private AzureCustomDomainRequest buildRequest(String hostName) {
        AzureCustomDomainRequest request = new AzureCustomDomainRequest();

        AzureCustomDomainRequest.TlsSettings tls =
                new AzureCustomDomainRequest.TlsSettings();
        tls.setCertificateType("ManagedCertificate");
        tls.setMinimumTlsVersion("TLS12");

        AzureCustomDomainRequest.Properties props =
                new AzureCustomDomainRequest.Properties();
        props.setHostName(hostName);
        props.setTlsSettings(tls);

        request.setProperties(props);
        return request;
    }

    private AzureAfdRouteRequest buildAfdRouteRequest(
            String originGroupName,
            String customDomainName
    ) {
        AzureAfdRouteRequest request = new AzureAfdRouteRequest();

        AzureAfdRouteRequest.Properties props =
                new AzureAfdRouteRequest.Properties();

        props.setOriginGroup(new AzureAfdRouteRequest.ResourceRef(
                String.format(
                        "/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Cdn/profiles/%s/originGroups/%s",
                        subscriptionId,
                        resourceGroup,
                        cdnProfile,
                        originGroupName
                )
        ));

        props.setCustomDomains(List.of(
                new AzureAfdRouteRequest.ResourceRef(
                        String.format(
                                "/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Cdn/profiles/%s/customDomains/%s",
                                subscriptionId,
                                resourceGroup,
                                cdnProfile,
                                customDomainName
                        )
                )
        ));

        props.setSupportedProtocols(List.of("Https"));
        props.setPatternsToMatch(List.of("/*"));
        props.setForwardingProtocol("HttpsOnly");
        props.setHttpsRedirect("Enabled");

        request.setProperties(props);
        return request;
    }



    private AzureAfdRouteRequest buildMergedAfdRouteRequest(
            String originGroupName,
            List<AzureAfdRouteRequest.ResourceRef> customDomains
    ) {

        AzureAfdRouteRequest request = new AzureAfdRouteRequest();
        AzureAfdRouteRequest.Properties properties =
                new AzureAfdRouteRequest.Properties();

        // Build Origin Group ID
        String originGroupId = String.format(
                "/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Cdn/profiles/%s/originGroups/%s",
                subscriptionId,
                resourceGroup,
                cdnProfile,
                originGroupName
        );

        properties.setOriginGroup(
                new AzureAfdRouteRequest.ResourceRef(originGroupId)
        );

        // Required Properties (must always be present in PUT)
        properties.setSupportedProtocols(List.of("Https"));
        properties.setPatternsToMatch(List.of("/*"));
        properties.setForwardingProtocol("HttpsOnly");
        properties.setHttpsRedirect("Enabled");

        // IMPORTANT: Full merged list
        properties.setCustomDomains(customDomains);

        request.setProperties(properties);

        return request;
    }



    public void updateRouteWithCustomDomainA1(
            String afdEndpointName,
            String routeName,
            String customDomainName
    ) {

        try {

            String accessToken = azureAuthService.fetchAccessToken().getAccessToken();

            String url = String.format(
                    "https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Cdn/profiles/%s/afdEndpoints/%s/routes/%s?api-version=%s",
                    subscriptionId,
                    resourceGroup,
                    cdnProfile,
                    afdEndpointName,
                    routeName,
                    apiVersion
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> getEntity = new HttpEntity<>(headers);

            // GET existing route
            ResponseEntity<AzureAfdRouteRequest> existingResponse =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            getEntity,
                            AzureAfdRouteRequest.class
                    );

            AzureAfdRouteRequest existingRoute = existingResponse.getBody();

            if (existingRoute == null) {
                throw new RuntimeException("Route not found in Azure");
            }

            List<AzureAfdRouteRequest.ResourceRef> domains =
                    new ArrayList<>(existingRoute.getProperties().getCustomDomains());

            // Add new domain only if not exists (case insensitive)
            boolean exists = domains.stream()
                    .anyMatch(d -> d.getId().equalsIgnoreCase(
                            String.format(
                                    "/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Cdn/profiles/%s/customDomains/%s",
                                    subscriptionId,
                                    resourceGroup,
                                    cdnProfile,
                                    customDomainName
                            )
                    ));

            if (!exists) {

                domains.add(new AzureAfdRouteRequest.ResourceRef(
                        String.format(
                                "/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Cdn/profiles/%s/customDomains/%s",
                                subscriptionId,
                                resourceGroup,
                                cdnProfile,
                                customDomainName
                        )
                ));
            }

            existingRoute.getProperties().setCustomDomains(domains);

            HttpEntity<AzureAfdRouteRequest> putEntity =
                    new HttpEntity<>(existingRoute, headers);

            // PUT full object back
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    putEntity,
                    Void.class
            );

            log.info("AFD route updated successfully");

        } catch (Exception ex) {
            log.error("Unexpected error while updating AFD route", ex);
            throw new RuntimeException("Azure AFD service error", ex);
        }
    }

}
