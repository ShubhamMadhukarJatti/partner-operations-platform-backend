package com.sharkdom.profilesection.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.profilesection.dto.*;
import com.sharkdom.profilesection.entity.OrganizationCertificationsConfig;
import com.sharkdom.profilesection.repository.CertificationsRepository;
import com.sharkdom.util.SharkdomPaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CertificationsService {

    private final CertificationsRepository repository;

    public CertificationResponse create(CertificationRequest request) {

        OrganizationCertificationsConfig entity = OrganizationCertificationsConfig.builder()
                .certificationName(request.getCertificationName())
                .logoUrl(request.getLogoUrl())
                .build();

        repository.save(entity);

        return mapToResponse(entity);
    }

    public CertificationResponse update(Long id, CertificationRequest request) {

        OrganizationCertificationsConfig entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH106));

        entity.setCertificationName(request.getCertificationName());
        entity.setLogoUrl(request.getLogoUrl());

        repository.save(entity);

        return mapToResponse(entity);
    }

    public void delete(Long id) {
        OrganizationCertificationsConfig entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH106));

        repository.delete(entity);
    }

    public CertificationResponse getById(Long id) {
        OrganizationCertificationsConfig entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH106));

        return mapToResponse(entity);
    }

    public SharkdomPaginatedResponse<CertificationResponse> getAll(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<OrganizationCertificationsConfig> pageRes = repository.findAll(pageable);

        SharkdomPaginatedResponse<CertificationResponse> response = new SharkdomPaginatedResponse<>();

        response.setContent(pageRes.getContent().stream().map(this::mapToResponse).toList());
        response.setPage(pageRes.getNumber());
        response.setSize(pageRes.getSize());
        response.setTotalElements(pageRes.getTotalElements());
        response.setTotalPages(pageRes.getTotalPages());
        response.setLast(pageRes.isLast());

        return response;
    }

    private CertificationResponse mapToResponse(OrganizationCertificationsConfig entity) {
        return CertificationResponse.builder()
                .id(entity.getId())
                .certificationName(entity.getCertificationName())
                .logoUrl(entity.getLogoUrl())
                .build();
    }
}