package com.sharkdom.service.organization;

import com.sharkdom.entity.organization.Organization;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class OrganizationCurrencyService {

    @Autowired
    private  OrganizationRepository organizationRepository;


    @Transactional
    public Map<String, Object> updateOrganizationCurrency(String currency) {
        log.info("OrganizationCurrencyService >> updateOrganizationCurrency >> called with currency={}", currency);

        Map<String, Object> response = new HashMap<>();

        if (!StringUtils.hasText(currency)) {
            log.warn("Invalid currency value provided: '{}'", currency);
            response.put("success", false);
            response.put("message", "Currency cannot be empty.");
            return response;
        }

        Long orgIdFromToken = Util.getOrgIdFromToken();
        log.debug("Fetched organization ID from token: {}", orgIdFromToken);

        Optional<Organization> optionalOrganization = organizationRepository.findById(orgIdFromToken);

        if (optionalOrganization.isEmpty()) {
            log.error("Organization not found for ID={}", orgIdFromToken);
            response.put("success", false);
            response.put("message", "Organization not found.");
            return response;
        }

        Organization organization = optionalOrganization.get();

        String previousCurrency = organization.getCurrency();
        organization.setCurrency(currency.trim().toUpperCase());
        organizationRepository.save(organization);

        log.info("Currency updated successfully for Organization ID={} | Previous={} | New={}",
                orgIdFromToken, previousCurrency, organization.getCurrency());

        response.put("success", true);
        response.put("message", "Currency updated successfully.");
        response.put("organizationId", orgIdFromToken);
        response.put("previousCurrency", previousCurrency);
        response.put("updatedCurrency", organization.getCurrency());

        return response;
    }

    public Map<String, Object> getOrganizationCurrency() {
        Map<String, Object> response = new HashMap<>();
        Long orgIdFromToken = Util.getOrgIdFromToken();
        log.debug("Fetching currency for Organization ID={}", orgIdFromToken);

        Optional<Organization> optionalOrganization = organizationRepository.findById(orgIdFromToken);

        if (optionalOrganization.isEmpty()) {
            log.error("Organization not found for ID={}", orgIdFromToken);
            response.put("success", false);
            response.put("message", "Organization not found.");
            return response;
        }

        Organization organization = optionalOrganization.get();
        response.put("success", true);
        response.put("organizationId", orgIdFromToken);
        response.put("currency", organization.getCurrency());

        return response;
    }


}
