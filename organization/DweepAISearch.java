package com.sharkdom.service.organization;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.dto.DweepSearchResponse;
import com.sharkdom.dto.SearchResponse;
import com.sharkdom.entity.organization.OrganizationCustomResponse;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.service.credits.CreditService;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DweepAISearch {

    @Autowired
    private CreditService creditService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private OrganizationService organizationService;

    public DweepSearchResponse searchOrganizations(String input) {
        log.info("searchOrganizations with input={}", input);
        Long orgId = Util.getOrgIdFromToken();

        // 1. Check credits
        if (!creditService.hasCredits(orgId)) {
            throw new ServiceException(
                    ErrorMessages.SH116, "Your credits are finished. Please upgrade to continue searching."
            );
        }


        // 2. Consume credit
        String consumeMessage = creditService.consumeCredit( orgId);

        // 3. Call external SearchService -> this will return subsectors
        SearchResponse aiSearchResponse = searchService.searchCompanies("AI", input);
        List<String> subsectors = aiSearchResponse.getSubsector();

        log.info("Subsectors from AI Search: {}", subsectors);

        // 4. Use subsectors as filters in OrganizationService
        Page<OrganizationCustomResponse> organizations = organizationService.searchOrganizationsByFilterDto(
                subsectors, false, 0, 1000);

        // 5. Build final response
        DweepSearchResponse response = new DweepSearchResponse();
        response.setMessage(consumeMessage);
        response.setOrganization(organizations);
        return response;
    }
}
