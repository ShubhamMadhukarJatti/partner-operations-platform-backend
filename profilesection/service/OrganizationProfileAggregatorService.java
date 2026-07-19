package com.sharkdom.profilesection.service;

import com.sharkdom.profilesection.dto.*;
import com.sharkdom.profilesection.entity.*;
import com.sharkdom.profilesection.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationProfileAggregatorService {

    private final OrganizationCertificationRepository certificationRepository;
    private final OrganizationResourceRepository resourceRepository;
    private final OrganizationPartnerProgramRepository partnerProgramRepository;

    public OrganizationProfileResponse getFullProfile(Long orgId) {

        log.info("[GET FULL PROFILE] orgId={}", orgId);

        // Certifications
        List<OrganizationCertificationResponse> certifications =
                certificationRepository.findAll()
                        .stream()
                        .filter(c -> c.getOrganizationId().equals(orgId))
                        .map(this::mapCertification)
                        .toList();

        // Resources
        List<OrganizationResourceResponse> resources =
                resourceRepository.findAll()
                        .stream()
                        .filter(r -> r.getOrganizationId().equals(orgId))
                        .map(this::mapResource)
                        .toList();

        // Partner Program
        OrganizationPartnerProgramResponse partnerProgram =
                partnerProgramRepository.findByOrganizationId(orgId)
                        .map(this::mapPartnerProgram)
                        .orElse(null);

        return OrganizationProfileResponse.builder()
                .certifications(certifications)
                .resources(resources)
                .partnerProgram(partnerProgram)
                .build();
    }

    private OrganizationCertificationResponse mapCertification(OrganizationCertification c) {
        return OrganizationCertificationResponse.builder()
                .id(c.getId())
                .certificationName(c.getCertificationName())
                .verificationUrl(c.getVerificationUrl())
                .status(c.getStatus())
                .submittedAt(c.getSubmittedAt())
                .verifiedAt(c.getVerifiedAt())
                .remarks(c.getRemarks())
                .build();
    }

    private OrganizationResourceResponse mapResource(OrganizationResource r) {
        return OrganizationResourceResponse.builder()
                .id(r.getId())
                .title(r.getTitle())
                .type(r.getType())
                .source(r.getSource())
                .url(r.getUrl())
                .build();
    }

    private OrganizationPartnerProgramResponse mapPartnerProgram(OrganizationPartnerProgram p) {
        return OrganizationPartnerProgramResponse.builder()
                .id(p.getId())
                .programName(p.getProgramName())
                .isActive(p.getIsActive())
                .programUrl(p.getProgramUrl())
                .benefits(p.getBenefits().stream()
                        .map(OrganizationPartnerProgramBenefit::getBenefit)
                        .toList())
                .build();
    }
}