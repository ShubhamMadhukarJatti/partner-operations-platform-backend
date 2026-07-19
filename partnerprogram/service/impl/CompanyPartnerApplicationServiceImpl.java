package com.sharkdom.partnerprogram.service.impl;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnerprogram.dtos.CompanyPartnerApplicationDTO;
import com.sharkdom.partnerprogram.entities.CompanyPartnerApplication;
import com.sharkdom.partnerprogram.repository.CompanyPartnerApplicationRepository;
import com.sharkdom.partnerprogram.service.CompanyPartnerApplicationService;
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
public class CompanyPartnerApplicationServiceImpl implements CompanyPartnerApplicationService {

    private final CompanyPartnerApplicationRepository repository;
    private final EmailService emailService;

    @Override
    public CompanyPartnerApplicationDTO create(CompanyPartnerApplicationDTO dto) {
        log.info("Creating CompanyPartnerApplication for email: {}", dto.getContactEmail());

        CompanyPartnerApplication entity = mapToEntity(dto);
        CompanyPartnerApplication savedEntity = repository.save(entity);

        try {
            // 1. Send email to internal team (Deepak / CSM)
            emailService.sendCompanyPartnerCreatedEmail(
                    "COMPANY_PARTNER_CREATED_TO_CSM",
                    savedEntity,
                    "deepak.v@sharkdom.com"
            );

            // 2. Send confirmation email to user
            emailService.sendTemplateWithUserName(
                    "COMPANY_PARTNER_CREATED",
                    savedEntity.getContactEmail(),
                    savedEntity.getCompanyName()
            );

        } catch (Exception e) {
            log.error("Error while sending email for CompanyPartnerApplication id: {}, error: {}",
                    savedEntity.getId(), e.getMessage(), e);
        }

        return mapToDTO(savedEntity);
    }

    @Override
    public CompanyPartnerApplicationDTO update(Long id, CompanyPartnerApplicationDTO dto) {
        log.info("Updating CompanyPartnerApplication with id: {}", id);

        CompanyPartnerApplication entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        updateEntity(entity, dto);
        return mapToDTO(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting CompanyPartnerApplication with id: {}", id);

        CompanyPartnerApplication entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        repository.delete(entity);
    }

    @Override
    public CompanyPartnerApplicationDTO getById(Long id) {
        log.info("Fetching CompanyPartnerApplication with id: {}", id);

        return repository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));
    }

    @Override
    public SharkdomPaginatedResponse<CompanyPartnerApplicationDTO> getAll(int page, int size) {
        log.info("Fetching CompanyPartnerApplications page: {}, size: {}", page, size);

        Page<CompanyPartnerApplication> pageData =
                repository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));

        SharkdomPaginatedResponse<CompanyPartnerApplicationDTO> response = new SharkdomPaginatedResponse<>();
        response.setContent(pageData.getContent().stream().map(this::mapToDTO).collect(Collectors.toList()));
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(pageData.getTotalElements());
        response.setTotalPages(pageData.getTotalPages());
        response.setLast(pageData.isLast());

        return response;
    }

    private CompanyPartnerApplication mapToEntity(CompanyPartnerApplicationDTO dto) {
        return CompanyPartnerApplication.builder()
                .companyName(dto.getCompanyName())
                .companyWebsite(dto.getCompanyWebsite())
                .primaryContactName(dto.getPrimaryContactName())
                .contactEmail(dto.getContactEmail())
                .companySize(dto.getCompanySize())
                .partnerType(dto.getPartnerType())
                .icpFitExplanation(dto.getIcpFitExplanation())
                .maturity(dto.getMaturity())
                .hasExistingRelationship(dto.getHasExistingRelationship())
                .informationConfirmed(dto.getInformationConfirmed())
                .agreedToTerms(dto.getAgreedToTerms())
                .status(dto.getStatus())
                .reviewComments(dto.getReviewComments())
                .build();
    }

    private void updateEntity(CompanyPartnerApplication entity, CompanyPartnerApplicationDTO dto) {
        entity.setCompanyName(dto.getCompanyName());
        entity.setCompanyWebsite(dto.getCompanyWebsite());
        entity.setPrimaryContactName(dto.getPrimaryContactName());
        entity.setContactEmail(dto.getContactEmail());
        entity.setCompanySize(dto.getCompanySize());
        entity.setPartnerType(dto.getPartnerType());
        entity.setIcpFitExplanation(dto.getIcpFitExplanation());
        entity.setMaturity(dto.getMaturity());
        entity.setHasExistingRelationship(dto.getHasExistingRelationship());
        entity.setInformationConfirmed(dto.getInformationConfirmed());
        entity.setAgreedToTerms(dto.getAgreedToTerms());
        entity.setStatus(dto.getStatus());
        entity.setReviewComments(dto.getReviewComments());
    }

    private CompanyPartnerApplicationDTO mapToDTO(CompanyPartnerApplication entity) {
        CompanyPartnerApplicationDTO dto = new CompanyPartnerApplicationDTO();
        dto.setId(entity.getId());
        dto.setCompanyName(entity.getCompanyName());
        dto.setCompanyWebsite(entity.getCompanyWebsite());
        dto.setPrimaryContactName(entity.getPrimaryContactName());
        dto.setContactEmail(entity.getContactEmail());
        dto.setCompanySize(entity.getCompanySize());
        dto.setPartnerType(entity.getPartnerType());
        dto.setIcpFitExplanation(entity.getIcpFitExplanation());
        dto.setMaturity(entity.getMaturity());
        dto.setHasExistingRelationship(entity.getHasExistingRelationship());
        dto.setInformationConfirmed(entity.getInformationConfirmed());
        dto.setAgreedToTerms(entity.getAgreedToTerms());
        dto.setStatus(entity.getStatus());
        dto.setReviewComments(entity.getReviewComments());
        return dto;
    }
}