package com.sharkdom.partnerattribution.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.fasterxml.jackson.databind.JsonNode;
import com.sharkdom.partnerattribution.dto.SalesforceDealData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesforceDataMappingService {

    private final RestTemplate restTemplate;
    @Autowired
    @Qualifier("salesforceWebsiteCache")
    private Cache<String, String> salesforceWebsiteCache;
    @Autowired
    @Qualifier("salesforceDealDataCache")
    private Cache<String, SalesforceDealData> salesforceDealDataCache;

    public String getWebsiteByOpportunityId(
            String instanceUrl,
            String opportunityId,
            String accessToken) {

        String cacheKey = instanceUrl + ":" + opportunityId;

        return salesforceWebsiteCache.get(
                cacheKey,
                key -> fetchWebsiteByOpportunityId(instanceUrl, opportunityId, accessToken)
        );
    }

    private String fetchWebsiteByOpportunityId(
            String instanceUrl,
            String opportunityId,
            String accessToken) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> entity =
                    new HttpEntity<>(headers);

            String soql =
                    "SELECT Account.Id, Account.Website " +
                            "FROM Opportunity " +
                            "WHERE Id = '" + opportunityId + "'";

            URI uri =
                    UriComponentsBuilder
                            .fromHttpUrl(instanceUrl + "/services/data/v61.0/query")
                            .queryParam("q", soql)
                            .build()
                            .encode()
                            .toUri();

            ResponseEntity<JsonNode> response =
                    restTemplate.exchange(
                            uri,
                            HttpMethod.GET,
                            entity,
                            JsonNode.class
                    );

            JsonNode records =
                    response.getBody().path("records");

            if (!records.isArray() || records.isEmpty()) {

                log.warn(
                        "No opportunity found for opportunityId={}",
                        opportunityId
                );

                return null;
            }

            JsonNode opportunity =
                    records.get(0);

            JsonNode account =
                    opportunity.path("Account");

            if (account.isMissingNode() || account.isNull()) {

                log.warn(
                        "No account associated with opportunityId={}",
                        opportunityId
                );

                return null;
            }

            String accountId =
                    account.path("Id").asText(null);

            String website =
                    account.path("Website").asText(null);

            log.info(
                    "Resolved website={} for opportunityId={} accountId={}",
                    website,
                    opportunityId,
                    accountId
            );

            return website;

        } catch (Exception ex) {

            log.error(
                    "Failed to fetch website for opportunityId={}",
                    opportunityId,
                    ex
            );

            return null;
        }
    }

    public SalesforceDealData getSalesforceDealData(
            String instanceUrl,
            String opportunityId,
            String accessToken) {

        String cacheKey = instanceUrl + ":" + opportunityId;

        return salesforceDealDataCache.get(
                cacheKey,
                key -> fetchSalesforceDealData(instanceUrl, opportunityId, accessToken)
        );
    }

    private SalesforceDealData fetchSalesforceDealData(
            String instanceUrl,
            String opportunityId,
            String accessToken) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> entity =
                    new HttpEntity<>(headers);

            String soql =
                    "SELECT StageName," +
                            " Account.NumberOfEmployees," +
                            " Account.Industry," +
                            " Account.BillingCountry " +
                            "FROM Opportunity " +
                            "WHERE Id='" + opportunityId + "'";

            URI uri =
                    UriComponentsBuilder
                            .fromHttpUrl(instanceUrl + "/services/data/v61.0/query")
                            .queryParam("q", soql)
                            .build()
                            .encode()
                            .toUri();

            ResponseEntity<JsonNode> response =
                    restTemplate.exchange(
                            uri,
                            HttpMethod.GET,
                            entity,
                            JsonNode.class
                    );

            JsonNode records =
                    response.getBody().path("records");

            if (!records.isArray() || records.isEmpty()) {

                log.warn(
                        "No opportunity found for opportunityId={}",
                        opportunityId
                );

                return null;
            }

            JsonNode record = records.get(0);

            JsonNode account = record.path("Account");

            return SalesforceDealData.builder()
                    .stage(record.path("StageName").asText(null))
                    .employees(
                            account.path("NumberOfEmployees").isMissingNode()
                                    ? null
                                    : account.path("NumberOfEmployees").asInt()
                    )
                    .industry(
                            account.path("Industry").asText(null)
                    )
                    .country(
                            account.path("BillingCountry").asText(null)
                    )
                    .build();

        } catch (Exception ex) {

            log.error(
                    "Failed to fetch Salesforce deal data for opportunityId={}",
                    opportunityId,
                    ex
            );

            return null;
        }
    }
}
