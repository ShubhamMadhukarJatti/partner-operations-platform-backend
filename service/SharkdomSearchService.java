package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.entity.PartnerCompanyProfile;
import com.sharkdom.agenticai.model.PartnerCompanyProfileSearchResponse;
import com.sharkdom.agenticai.model.QueryRequest;
import com.sharkdom.agenticai.model.QueryResponse;
import com.sharkdom.agenticai.repository.PartnerCompanyProfileRepository;
import com.sharkdom.agenticai.specification.PartnerCompanyProfileSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SharkdomSearchService {

    private final RestTemplate restTemplate;
    private final PartnerCompanyProfileRepository profileRepository;

    private static final String SEARCH_API_URL =
            "https://sharkdom-search-cdd5a2h0ftgbfqah.centralindia-01.azurewebsites.net/query";

    public QueryResponse queryPartners(QueryRequest request) {

        log.info("Calling Sharkdom Search API with sector: {}", request.getSector());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<QueryRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<QueryResponse> response =
                restTemplate.exchange(
                        SEARCH_API_URL,
                        HttpMethod.POST,
                        entity,
                        QueryResponse.class
                );

        log.info("Received response from Sharkdom Search API");

        return response.getBody();
    }

    public Page<PartnerCompanyProfileSearchResponse> searchByFilter(
            List<String> subsectors,
            List<String> compliances,
            String keyword,
            int page,
            int size) {

        Specification<PartnerCompanyProfile> spec = Specification
                .where(PartnerCompanyProfileSpecification.hasSubsectorIn(subsectors))
                .and(PartnerCompanyProfileSpecification.hasComplianceIn(compliances))
                .and(PartnerCompanyProfileSpecification.hasKeyword(keyword));

        Page<PartnerCompanyProfile> result =
                profileRepository.findAll(spec, PageRequest.of(page, size));

        return result.map(entity -> {
            PartnerCompanyProfileSearchResponse dto =
                    new PartnerCompanyProfileSearchResponse();
            dto.setAbout(entity.getAbout());
            dto.setId(entity.getId());
            dto.setCompanyName(entity.getCompanyName());
            dto.setSubsectors(entity.getSubsectors());
            dto.setCompliances(entity.getCompliances());
            dto.setDescription(entity.getDescription());
            dto.setWebsite(entity.getWebsite());
            dto.setAvgPartnerSourceRevenue(entity.getAvgPartnerSourceRevenue());
            dto.setPartnerRange(entity.getPartnerRange());
            dto.setCompliances(entity.getCompliances());
            dto.setPartnerShipTeam(entity.getPartnerShipTeam());

            return dto;
        });
    }


}
