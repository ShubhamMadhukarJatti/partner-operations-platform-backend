package com.sharkdom.profilesection.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.profilesection.dto.*;
import com.sharkdom.profilesection.entity.OrganizationCertification;
import com.sharkdom.profilesection.enums.VerificationStatus;
import com.sharkdom.profilesection.repository.OrganizationCertificationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.util.SharkdomPaginatedResponse;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationCertificationService {

    private final OrganizationCertificationRepository repository;
    private final OrganizationRepository organizationRepository;

    public OrganizationCertificationResponse createCertification(OrganizationCertificationRequest request) {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[CREATE CERTIFICATION] orgId={} request={}", orgId, request);

        OrganizationCertification entity = OrganizationCertification.builder()
                .organizationId(orgId)
                .certificateId(request.getCertificationId())
                .certificationName(request.getCertificationName())
                .verificationUrl(request.getVerificationUrl())
                .status(VerificationStatus.PENDING)
                .build();

        repository.save(entity);

        return mapToResponse(entity);
    }

    public OrganizationCertificationResponse updateCertification(Long id, OrganizationCertificationRequest request) {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[UPDATE CERTIFICATION] orgId={} id={}", orgId, id);

        OrganizationCertification entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        if (!entity.getOrganizationId().equals(orgId)) {
            throw new ServiceException(ErrorMessages.SH13);
        }
        entity.setCertificateId(request.getCertificationId());
        entity.setCertificationName(request.getCertificationName());
        entity.setVerificationUrl(request.getVerificationUrl());
        entity.setStatus(VerificationStatus.PENDING);

        repository.save(entity);

        return mapToResponse(entity);
    }

    public void deleteCertification(Long id) {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[DELETE CERTIFICATION] orgId={} id={}", orgId, id);

        OrganizationCertification entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        if (!entity.getOrganizationId().equals(orgId)) {
            throw new ServiceException(ErrorMessages.SH13);
        }

        repository.delete(entity);
    }

    public OrganizationCertificationResponse getCertification(Long id) {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[GET CERTIFICATION] orgId={} id={}", orgId, id);

        OrganizationCertification entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        if (!entity.getOrganizationId().equals(orgId)) {
            throw new ServiceException(ErrorMessages.SH13);
        }

        return mapToResponse(entity);
    }


    private OrganizationCertificationResponse mapToResponse(OrganizationCertification entity) {
        return OrganizationCertificationResponse.builder()
                .id(entity.getId())
                .certificationName(entity.getCertificationName())
                .verificationUrl(entity.getVerificationUrl())
                .status(entity.getStatus())
                .certificateId(entity.getCertificateId())
                .submittedAt(entity.getSubmittedAt())
                .verifiedAt(entity.getVerifiedAt())
                .remarks(entity.getRemarks())
                .build();
    }

    private SharkdomPaginatedResponse<OrganizationCertificationResponse> mapToPaginatedResponse(Page<OrganizationCertification> page) {

        SharkdomPaginatedResponse<OrganizationCertificationResponse> response = new SharkdomPaginatedResponse<>();

        response.setContent(page.getContent().stream().map(this::mapToResponse).toList());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());

        return response;
    }

    public List<OrganizationCertificationResponse> getByOrgId(Long orgId) {
        return repository.findByOrganizationIdAndStatus(orgId,VerificationStatus.VERIFIED)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<OrganizationCertificationResponse> getByOrgIdPending(Long orgId) {
        return repository.findByOrganizationIdAndStatus(orgId,VerificationStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<OrganizationCertificationResponse> getByOrgIdAll(Long orgId) {
        return repository.findByOrganizationId(orgId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrganizationCertificationResponse updateVerificationStatus(
            Long id,
            CertificationStatusUpdateRequest request) {

        log.info("[ADMIN VERIFY CERTIFICATION] id={} request={}", id, request);

        OrganizationCertification entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        entity.setStatus(request.getStatus());
        entity.setRemarks(request.getRemarks());

        if (request.getStatus() == VerificationStatus.VERIFIED) {

            entity.setVerifiedAt(LocalDateTime.now());

            // ---------- UPDATE ORGANIZATION COMPLIANCES ----------
            Organization organization = organizationRepository.findById(entity.getId()).get();

            if (organization != null) {

                List<String> compliances = organization.getCompliances();

                if (compliances == null) {
                    compliances = new ArrayList<>();
                }

                String certificationName = entity.getCertificationName();

                if (certificationName != null && !compliances.contains(certificationName)) {
                    compliances.add(certificationName);
                }

                organization.setCompliances(compliances);
            }

        } else {
            entity.setVerifiedAt(null);
        }

        repository.save(entity);

        return mapToResponse(entity);
    }

    public SharkdomPaginatedResponse<OrganizationCertificationResponse> getAllPendingCertifications(
            int page,
            int size) {

        log.info("[ADMIN GET PENDING CERTIFICATIONS] page={} size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<OrganizationCertification> pageRes =
                repository.findByStatus(VerificationStatus.PENDING, pageable);

        return mapToPaginatedResponse(pageRes);
    }

    public List<String> getVerifiedCertificationNamesByOrgId(Long orgId) {

        log.info("[GET VERIFIED CERTIFICATION NAMES] orgId={}", orgId);

        List<String> certificationNames = repository.findVerifiedCertificationNames(orgId);

        if (certificationNames == null || certificationNames.isEmpty()) {
            log.info("No verified certifications found for orgId={}", orgId);
            return List.of();
        }

        return certificationNames;
    }
}