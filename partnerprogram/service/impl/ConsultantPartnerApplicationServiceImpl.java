package com.sharkdom.partnerprogram.service.impl;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnerprogram.dtos.ConsultantPartnerApplicationDTO;
import com.sharkdom.partnerprogram.entities.ConsultantPartnerApplication;
import com.sharkdom.partnerprogram.repository.ConsultantPartnerApplicationRepository;
import com.sharkdom.partnerprogram.service.ConsultantPartnerApplicationService;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.util.SharkdomPaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultantPartnerApplicationServiceImpl implements ConsultantPartnerApplicationService {

    private final ConsultantPartnerApplicationRepository repository;
    private final EmailService emailService;

    @Override
    public ConsultantPartnerApplicationDTO create(ConsultantPartnerApplicationDTO dto) {
        log.info("Creating ConsultantPartnerApplication for email: {}", dto.getEmail());

        ConsultantPartnerApplication entity = mapToEntity(dto);
        ConsultantPartnerApplication savedEntity = repository.save(entity);

        try {
            // 1. Internal email (Deepak / CSM)
            emailService.sendConsultantPartnerCreatedEmail(
                    "CONSULTANT_PARTNER_CREATED_TO_CSM",
                    savedEntity,
                    "deepak.v@sharkdom.com"
            );

            // 2. User confirmation email
            emailService.sendTemplateWithUserName(
                    "CONSULTANT_PARTNER_CREATED",
                    savedEntity.getEmail(),
                    savedEntity.getFullName()
            );

        } catch (Exception e) {
            log.error("Error while sending email for ConsultantPartnerApplication id: {}, error: {}",
                    savedEntity.getId(), e.getMessage(), e);
        }

        return mapToDTO(savedEntity);
    }

    @Override
    public ConsultantPartnerApplicationDTO update(Long id, ConsultantPartnerApplicationDTO dto) {
        log.info("Updating ConsultantPartnerApplication with id: {}", id);

        ConsultantPartnerApplication entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        updateEntity(entity, dto);
        return mapToDTO(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting ConsultantPartnerApplication with id: {}", id);

        ConsultantPartnerApplication entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        repository.delete(entity);
    }

    @Override
    public ConsultantPartnerApplicationDTO getById(Long id) {
        log.info("Fetching ConsultantPartnerApplication with id: {}", id);

        return repository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));
    }

    @Override
    public SharkdomPaginatedResponse<ConsultantPartnerApplicationDTO> getAll(int page, int size) {
        log.info("Fetching ConsultantPartnerApplications page: {}, size: {}", page, size);

        Page<ConsultantPartnerApplication> pageData =
                repository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));

        SharkdomPaginatedResponse<ConsultantPartnerApplicationDTO> response = new SharkdomPaginatedResponse<>();
        response.setContent(
                pageData.getContent()
                        .stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList())
        );
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(pageData.getTotalElements());
        response.setTotalPages(pageData.getTotalPages());
        response.setLast(pageData.isLast());

        return response;
    }

    private ConsultantPartnerApplication mapToEntity(ConsultantPartnerApplicationDTO dto) {
        return ConsultantPartnerApplication.builder()
                .fullName(dto.getFullName())
                .linkedinProfileUrl(dto.getLinkedinProfileUrl())
                .email(dto.getEmail())
                .country(dto.getCountry())
                .roleDescription(dto.getRoleDescription())
                .advisoryCount(dto.getAdvisoryCount())
                .arrRange(dto.getArrRange())
                .typicalClientArrRange(dto.getTypicalClientArrRange())
                .partnerProgramStatus(dto.getPartnerProgramStatus())
                .leadSource(dto.getLeadSource())
                .useDweepBot(dto.getUseDweepBot())
                .acceptCommissionTerms(dto.getAcceptCommissionTerms())
                .agreeToTerms(dto.getAgreeToTerms())
                .build();
    }

    private void updateEntity(ConsultantPartnerApplication entity, ConsultantPartnerApplicationDTO dto) {
        entity.setFullName(dto.getFullName());
        entity.setLinkedinProfileUrl(dto.getLinkedinProfileUrl());
        entity.setEmail(dto.getEmail());
        entity.setCountry(dto.getCountry());
        entity.setRoleDescription(dto.getRoleDescription());
        entity.setAdvisoryCount(dto.getAdvisoryCount());
        entity.setArrRange(dto.getArrRange());
        entity.setTypicalClientArrRange(dto.getTypicalClientArrRange());
        entity.setPartnerProgramStatus(dto.getPartnerProgramStatus());
        entity.setLeadSource(dto.getLeadSource());
        entity.setUseDweepBot(dto.getUseDweepBot());
        entity.setAcceptCommissionTerms(dto.getAcceptCommissionTerms());
        entity.setAgreeToTerms(dto.getAgreeToTerms());
    }

    private ConsultantPartnerApplicationDTO mapToDTO(ConsultantPartnerApplication entity) {
        ConsultantPartnerApplicationDTO dto = new ConsultantPartnerApplicationDTO();

        dto.setId(entity.getId());
        dto.setFullName(entity.getFullName());
        dto.setLinkedinProfileUrl(entity.getLinkedinProfileUrl());
        dto.setEmail(entity.getEmail());
        dto.setCountry(entity.getCountry());
        dto.setRoleDescription(entity.getRoleDescription());
        dto.setAdvisoryCount(entity.getAdvisoryCount());
        dto.setArrRange(entity.getArrRange());
        dto.setTypicalClientArrRange(entity.getTypicalClientArrRange());
        dto.setPartnerProgramStatus(entity.getPartnerProgramStatus());
        dto.setLeadSource(entity.getLeadSource());
        dto.setUseDweepBot(entity.getUseDweepBot());
        dto.setAcceptCommissionTerms(entity.getAcceptCommissionTerms());
        dto.setAgreeToTerms(entity.getAgreeToTerms());

        return dto;
    }
}