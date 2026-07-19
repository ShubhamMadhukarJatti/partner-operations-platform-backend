package com.sharkdom.partnerattribution.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZohoDataMappingService {

    private final RestTemplate restTemplate;

    public String getWebsiteByDealId(
            Long organizationId,
            String dealId,
            String accessToken) {

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization",
                    "Zoho-oauthtoken " + accessToken);

            HttpEntity<Void> entity =
                    new HttpEntity<>(headers);

            /**
             * Fetch Deal
             */
            ResponseEntity<JsonNode> dealResponse =
                    restTemplate.exchange(
                            "https://www.zohoapis.in/crm/v8/Deals/" + dealId,
                            HttpMethod.GET,
                            entity,
                            JsonNode.class
                    );

            JsonNode dealData =
                    dealResponse.getBody()
                            .path("data")
                            .get(0);

            if (dealData == null) {
                log.warn(
                        "No deal found for dealId={}",
                        dealId
                );
                return null;
            }

            JsonNode accountNode =
                    dealData.path("Account_Name");

            String accountId =
                    accountNode.path("id").asText();

            if (accountId == null || accountId.isBlank()) {

                log.warn(
                        "No account associated with dealId={}",
                        dealId
                );

                return null;
            }

            /**
             * Fetch Account
             */
            ResponseEntity<JsonNode> accountResponse =
                    restTemplate.exchange(
                            "https://www.zohoapis.in/crm/v8/Accounts/" + accountId,
                            HttpMethod.GET,
                            entity,
                            JsonNode.class
                    );

            JsonNode accountData =
                    accountResponse.getBody()
                            .path("data")
                            .get(0);

            if (accountData == null) {
                return null;
            }

            String website =
                    accountData.path("Website")
                            .asText(null);

            log.info(
                    "Resolved website={} for dealId={} accountId={}",
                    website,
                    dealId,
                    accountId
            );

            return website;

        } catch (Exception ex) {

            log.error(
                    "Failed to fetch website for dealId={}",
                    dealId,
                    ex
            );

            return null;
        }
    }
}