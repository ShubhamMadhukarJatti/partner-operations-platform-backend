package com.sharkdom.salesforce.service;

import com.azure.json.implementation.jackson.core.JsonProcessingException;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.salesforce.dto.SalesforceDescribeResponse;
import com.sharkdom.salesforce.dto.SalesforceQueryRequest;
import com.sharkdom.salesforce.dto.SalesforceQueryResponse;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesforceService {

    private final SalesforceDescribeService salesforceDescribeService;
    private final SalesforceContactService salesforceContactService;
    private final SalesforceAuthService salesforceAuthService;
    private final IntegrationRepository integrationRepository;
    private final SalesforceOpportunityDescribeService salesforceOpportunityDescribeService;
    private final SalesforceOpportunityService salesforceOpportunityService;
    private final SalesforceAccountService salesforceAccountService;
    private final SalesforceAccountDescribeService salesforceAccountDescribeService;

    @Transactional
    public SalesforceQueryResponse getContacts(List<String> fields) {
        log.info("getContacts");

        Long organizationId = Util.getOrgIdFromToken();
        log.info("organizationId: {}", organizationId);

        var details = integrationRepository.findByOrganizationIdAndIntegrationType(
                organizationId, IntegrationType.SALESFORCE
        );

        var refreshToken = details.getRefreshToken();
        var token = salesforceAuthService.refreshAccessToken(refreshToken);

        SalesforceQueryRequest queryRequest = new SalesforceQueryRequest();
        queryRequest.setFields(fields);

        return salesforceContactService.fetchRecentContacts(
                token.instanceUrl(),
                token.accessToken(),
                queryRequest
        );
    }


    @Transactional
    public SalesforceDescribeResponse getDescription() throws JsonProcessingException, com.fasterxml.jackson.core.JsonProcessingException {
        log.info("getDescription");
        Long organizationId = Util.getOrgIdFromToken();
        log.info("organizationId: {}", organizationId);
        var details = integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.SALESFORCE);
        var refreshToken = details.getRefreshToken();
        var token = salesforceAuthService.refreshAccessToken(refreshToken);
        return salesforceDescribeService.describeContact(token.instanceUrl(),token.accessToken());
    }


    @Transactional
    public SalesforceQueryResponse getContactsByUserId(String userId,List<String> fields) {
        log.info("getContacts");
        log.info("userId: {}", userId);

        var details = integrationRepository.findByUserIdAndIntegrationType(
                userId, IntegrationType.SALESFORCE
        );

        var refreshToken = details.getRefreshToken();
        var token = salesforceAuthService.refreshAccessTokenForEPS(refreshToken);

        SalesforceQueryRequest queryRequest = new SalesforceQueryRequest();
        queryRequest.setFields(fields);

        return salesforceContactService.fetchRecentContacts(
                token.instanceUrl(),
                token.accessToken(),
                queryRequest
        );
    }

    @Transactional
    public SalesforceDescribeResponse getDescriptionByUserId(String userId) throws JsonProcessingException, com.fasterxml.jackson.core.JsonProcessingException {
        log.info("getDescription");
        log.info("userId: {}", userId);
        var details = integrationRepository.findByUserIdAndIntegrationType(userId, IntegrationType.SALESFORCE);
        var refreshToken = details.getRefreshToken();
        var token = salesforceAuthService.refreshAccessToken(refreshToken);
        return salesforceDescribeService.describeContact(token.instanceUrl(),token.accessToken());
    }

    @Transactional
    public Map<Object, Object> getDetails(Long organizationId, List<String> fields) {

        log.info("[SALESFORCE_FETCH_DETAILS] orgId={} fields={}", organizationId, fields);

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.SALESFORCE
                );

        if (details == null) {
            log.warn("[SALESFORCE_FETCH_DETAILS] No integration found orgId={}", organizationId);
            return Map.of();
        }

        var refreshToken = details.getRefreshToken();

        if (refreshToken == null) {
            log.warn("[SALESFORCE_FETCH_DETAILS] Refresh token missing orgId={}", organizationId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessToken(refreshToken);

        SalesforceQueryRequest queryRequest = new SalesforceQueryRequest();
        queryRequest.setFields(fields);

        SalesforceQueryResponse response =
                salesforceContactService.fetchRecentContacts(
                        token.instanceUrl(),
                        token.accessToken(),
                        queryRequest
                );

        if (response == null) {
            log.warn("[SALESFORCE_FETCH_DETAILS] Empty response orgId={}", organizationId);
            return Map.of();
        }

        return Map.of(
                "totalSize", response.totalSize(),
                "records", response.records(),
                "done", response.done()
        );
    }

    @Transactional
    public Map<String, Object> getAccounts(List<String> fields) {

        log.info("getAccounts");

        Long orgId = Util.getOrgIdFromToken();

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(orgId, IntegrationType.SALESFORCE);

        if (details == null || details.getRefreshToken() == null) {
            log.warn("No Salesforce integration found for orgId={}", orgId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessToken(details.getRefreshToken());

        return salesforceAccountService.fetchAccounts(
                token.instanceUrl(),
                token.accessToken(),
                fields,
                1000
        );
    }

    @Transactional
    public Map<String, Object> getAccountDescription() {

        log.info("getAccountDescription");

        Long orgId = Util.getOrgIdFromToken();

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(orgId, IntegrationType.SALESFORCE);

        if (details == null || details.getRefreshToken() == null) {
            log.warn("No Salesforce integration found for orgId={}", orgId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessToken(details.getRefreshToken());

        return salesforceAccountDescribeService.describeAccount(
                token.instanceUrl(),
                token.accessToken()
        );
    }

    @Transactional
    public Map<String, Object> getAccountDescriptionByUserId(String userId) {

        log.info("getAccountDescriptionByUserId userId={}", userId);

        var details = integrationRepository
                .findByUserIdAndIntegrationType(userId, IntegrationType.SALESFORCE);

        if (details == null || details.getRefreshToken() == null) {
            log.warn("No Salesforce integration found for userId={}", userId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessToken(details.getRefreshToken());

        return salesforceAccountDescribeService.describeAccount(
                token.instanceUrl(),
                token.accessToken()
        );
    }

    @Transactional
    public Map<String, Object> getAccountsByUserId(String userId, List<String> fields) {

        log.info("getAccountsByUserId userId={}", userId);

        var details = integrationRepository
                .findByUserIdAndIntegrationType(userId, IntegrationType.SALESFORCE);

        if (details == null || details.getRefreshToken() == null) {
            log.warn("No Salesforce integration found for userId={}", userId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessTokenForEPS(details.getRefreshToken());

        return salesforceAccountService.fetchAccounts(
                token.instanceUrl(),
                token.accessToken(),
                fields,
                1000
        );
    }

    @Transactional
    public Map<String, Object> getOpportunityDescriptionByUserId(String userId) {

        log.info("getOpportunityDescriptionByUserId userId={}", userId);

        var details = integrationRepository
                .findByUserIdAndIntegrationType(userId, IntegrationType.SALESFORCE);

        if (details == null || details.getRefreshToken() == null) {
            log.warn("No Salesforce integration found for userId={}", userId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessToken(details.getRefreshToken());

        return salesforceOpportunityDescribeService.describeOpportunity(
                token.instanceUrl(),
                token.accessToken()
        );
    }

    @Transactional
    public Map<String, Object> getOpportunitiesByUserId(String userId, List<String> fields) {

        log.info("getOpportunitiesByUserId userId={}", userId);

        var details = integrationRepository
                .findByUserIdAndIntegrationType(userId, IntegrationType.SALESFORCE);

        if (details == null || details.getRefreshToken() == null) {
            log.warn("No Salesforce integration found for userId={}", userId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessTokenForEPS(details.getRefreshToken());

        return salesforceOpportunityService.fetchOpportunities(
                token.instanceUrl(),
                token.accessToken(),
                new SalesforceQueryRequest(fields),
                1000
        );
    }

    @Transactional
    public Map<String, Object> getOpportunities(List<String> fields) {

        log.info("getOpportunities");

        Long orgId = Util.getOrgIdFromToken();

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(orgId, IntegrationType.SALESFORCE);

        if (details == null || details.getRefreshToken() == null) {
            log.warn("No Salesforce integration found for orgId={}", orgId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessToken(details.getRefreshToken());

        return salesforceOpportunityService.fetchOpportunities(
                token.instanceUrl(),
                token.accessToken(),
                new SalesforceQueryRequest(fields),
                1000
        );
    }

    @Transactional
    public Map<String, Object> getOpportunityDescription() {

        log.info("getOpportunityDescription");

        Long orgId = Util.getOrgIdFromToken();

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(orgId, IntegrationType.SALESFORCE);

        if (details == null || details.getRefreshToken() == null) {
            log.warn("No Salesforce integration found for orgId={}", orgId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessToken(details.getRefreshToken());

        return salesforceOpportunityDescribeService.describeOpportunity(
                token.instanceUrl(),
                token.accessToken()
        );
    }

    @Transactional
    public Map<String, Object> getOpportunityById(String opportunityId) {

        log.info("getOpportunityById opportunityId={}", opportunityId);

        Long orgId = Util.getOrgIdFromToken();

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        orgId,
                        IntegrationType.SALESFORCE
                );

        if (details == null || details.getRefreshToken() == null) {
            log.warn("No Salesforce integration found for orgId={}", orgId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessToken(
                details.getRefreshToken()
        );

        return salesforceOpportunityDescribeService.getOpportunityById(
                token.instanceUrl(),
                token.accessToken(),
                opportunityId
        );
    }

    @Transactional
    public Map<String, Object> getOpportunityByIdByUserId(
            String userId,
            String opportunityId
    ) {

        log.info(
                "getOpportunityByIdByUserId userId={} opportunityId={}",
                userId,
                opportunityId
        );

        var details = integrationRepository
                .findByUserIdAndIntegrationType(
                        userId,
                        IntegrationType.SALESFORCE
                );

        if (details == null || details.getRefreshToken() == null) {
            log.warn("No Salesforce integration found for userId={}", userId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessTokenForEPS(
                details.getRefreshToken()
        );

        return salesforceOpportunityDescribeService.getOpportunityById(
                token.instanceUrl(),
                token.accessToken(),
                opportunityId
        );
    }

    @Transactional
    public Map<String, Object> getContactById(String contactId) {

        log.info("getContactById contactId={}", contactId);

        Long orgId = Util.getOrgIdFromToken();

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        orgId,
                        IntegrationType.SALESFORCE
                );

        if (details == null || details.getRefreshToken() == null) {
            log.warn("No Salesforce integration found for orgId={}", orgId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessToken(
                details.getRefreshToken()
        );

        return salesforceContactService.getContactById(
                token.instanceUrl(),
                token.accessToken(),
                contactId
        );
    }

    @Transactional
    public Map<String, Object> getContactByIdByUserId(
            String userId,
            String contactId
    ) {

        log.info(
                "getContactByIdByUserId userId={} contactId={}",
                userId,
                contactId
        );

        var details = integrationRepository
                .findByUserIdAndIntegrationType(
                        userId,
                        IntegrationType.SALESFORCE
                );

        if (details == null || details.getRefreshToken() == null) {
            log.warn("No Salesforce integration found for userId={}", userId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessTokenForEPS(
                details.getRefreshToken()
        );

        return salesforceContactService.getContactById(
                token.instanceUrl(),
                token.accessToken(),
                contactId
        );
    }

    @Transactional
    public Map<String, Object> getAccountById(String accountId) {

        log.info("getAccountById accountId={}", accountId);

        Long orgId = Util.getOrgIdFromToken();

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        orgId,
                        IntegrationType.SALESFORCE
                );

        if (details == null || details.getRefreshToken() == null) {
            log.warn("No Salesforce integration found for orgId={}", orgId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessToken(
                details.getRefreshToken()
        );

        return salesforceAccountService.getAccountById(
                token.instanceUrl(),
                token.accessToken(),
                accountId
        );
    }

    @Transactional
    public Map<String, Object> getAccountByIdByUserId(
            String userId,
            String accountId
    ) {

        log.info(
                "getAccountByIdByUserId userId={} accountId={}",
                userId,
                accountId
        );

        var details = integrationRepository
                .findByUserIdAndIntegrationType(
                        userId,
                        IntegrationType.SALESFORCE
                );

        if (details == null || details.getRefreshToken() == null) {
            log.warn("No Salesforce integration found for userId={}", userId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessTokenForEPS(
                details.getRefreshToken()
        );

        return salesforceAccountService.getAccountById(
                token.instanceUrl(),
                token.accessToken(),
                accountId
        );
    }

    @Transactional
    public Map<String, Object> getFullContactObjectById(String contactId) {

        log.info("getContactById contactId={}", contactId);

        Long orgId = Util.getOrgIdFromToken();

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        orgId,
                        IntegrationType.SALESFORCE
                );

        if (details == null || details.getRefreshToken() == null) {
            log.warn("No Salesforce integration found for orgId={}", orgId);
            return Map.of();
        }

        var token = salesforceAuthService.refreshAccessToken(
                details.getRefreshToken()
        );

        return salesforceContactService.getFullContactById(
                token.instanceUrl(),
                token.accessToken(),
                contactId
        );
    }
}
