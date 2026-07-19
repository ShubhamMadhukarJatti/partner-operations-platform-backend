package com.sharkdom.service.ppi;

import com.sharkdom.model.ppi.AzureTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AzureAuthService {

    private final RestTemplate restTemplate;

    @Value("${azure.tenant-id}")
    private String tenantId;

    @Value("${azure.client-id}")
    private String clientId;

    @Value("${azure.client-secret}")
    private String clientSecret;

    @Value("${azure.scope}")
    private String scope;

    public AzureAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AzureTokenResponse fetchAccessToken() {
        try {
            String tokenUrl = "https://login.microsoftonline.com/"
                    + tenantId
                    + "/oauth2/v2.0/token";

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "client_credentials");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("scope", scope);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(formData, headers);

            log.info("Requesting Azure access token for tenant: {}", tenantId);

            ResponseEntity<AzureTokenResponse> response =
                    restTemplate.exchange(
                            tokenUrl,
                            HttpMethod.POST,
                            request,
                            AzureTokenResponse.class
                    );

            return response.getBody();

        } catch (Exception ex) {
            log.error("Unexpected error while fetching Azure token", ex);
            throw new RuntimeException("Azure token service error", ex);
        }
    }
}
