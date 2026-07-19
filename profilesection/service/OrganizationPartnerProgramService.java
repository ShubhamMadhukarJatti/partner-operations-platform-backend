package com.sharkdom.profilesection.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.profilesection.dto.*;
import com.sharkdom.profilesection.entity.OrganizationPartnerProgram;
import com.sharkdom.profilesection.entity.OrganizationPartnerProgramBenefit;
import com.sharkdom.profilesection.repository.OrganizationPartnerProgramRepository;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationPartnerProgramService {

    private final OrganizationPartnerProgramRepository repository;

    public OrganizationPartnerProgramResponse upsertProgram(OrganizationPartnerProgramRequest request) {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[UPSERT PARTNER PROGRAM] orgId={} request={}", orgId, request);

        OrganizationPartnerProgram program = repository.findByOrganizationId(orgId)
                .orElse(OrganizationPartnerProgram.builder()
                        .organizationId(orgId)
                        .build());

        program.setProgramName(request.getProgramName());
        program.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        program.setProgramUrl(request.getProgramUrl());

        // Handle benefits (replace old)
        program.getBenefits().clear();

        List<OrganizationPartnerProgramBenefit> benefitEntities = new ArrayList<>();

        if (request.getBenefits() != null) {
            for (String benefitText : request.getBenefits()) {
                OrganizationPartnerProgramBenefit benefit = OrganizationPartnerProgramBenefit.builder()
                        .benefit(benefitText)
                        .program(program)
                        .build();
                benefitEntities.add(benefit);
            }
        }

        program.setBenefits(benefitEntities);

        repository.save(program);

        return mapToResponse(program);
    }

    public OrganizationPartnerProgramResponse getProgram() {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[GET PARTNER PROGRAM] orgId={}", orgId);

        OrganizationPartnerProgram program = repository.findByOrganizationId(orgId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH106));

        return mapToResponse(program);
    }

    public void toggleProgramStatus(Boolean isActive) {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[TOGGLE PROGRAM STATUS] orgId={} isActive={}", orgId, isActive);

        OrganizationPartnerProgram program = repository.findByOrganizationId(orgId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH106));

        program.setIsActive(isActive);
        repository.save(program);
    }

    private OrganizationPartnerProgramResponse mapToResponse(OrganizationPartnerProgram program) {

        List<String> benefits = program.getBenefits()
                .stream()
                .map(OrganizationPartnerProgramBenefit::getBenefit)
                .toList();

        return OrganizationPartnerProgramResponse.builder()
                .id(program.getId())
                .programName(program.getProgramName())
                .isActive(program.getIsActive())
                .programUrl(program.getProgramUrl())
                .benefits(benefits)
                .build();
    }

    public OrganizationPartnerProgramResponse getByOrgId(Long orgId) {

        log.info("[GET PARTNER PROGRAM BY ORG] orgId={}", orgId);

        OrganizationPartnerProgram program = repository.findByOrganizationId(orgId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH106));

        return mapToResponse(program);
    }
}