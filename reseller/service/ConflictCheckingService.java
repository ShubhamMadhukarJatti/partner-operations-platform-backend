package com.sharkdom.reseller.service;

import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.deals.model.ConnectedAccountsResponse;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.repository.organization.IntegrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConflictCheckingService {

    private final IntegrationRepository integrationRepository;

    public void checkConnectedCRM(Long organizationId) {
        log.info("Checking for existing connected CRM integrations for organizationId={}", organizationId);
        var integrationDetailsList = integrationRepository.findAllByOrganizationIdAndRefreshTokenIsNotNull(organizationId);
        for (IntegrationDetails integrationDetails : integrationDetailsList) {

        }
    }
}
