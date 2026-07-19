package com.sharkdom.profilesection.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.profilesection.dto.*;
import com.sharkdom.profilesection.entity.OrganizationResource;
import com.sharkdom.profilesection.repository.OrganizationResourceRepository;
import com.sharkdom.util.SharkdomPaginatedResponse;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationResourceService {

    private final OrganizationResourceRepository repository;

    public OrganizationResourceResponse create(OrganizationResourceRequest request) {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[CREATE RESOURCE] orgId={} request={}", orgId, request);

        OrganizationResource entity = OrganizationResource.builder()
                .organizationId(orgId)
                .title(request.getTitle())
                .type(request.getType())
                .source(request.getSource())
                .url(request.getUrl())
                .build();

        repository.save(entity);

        return mapToResponse(entity);
    }

    public OrganizationResourceResponse update(Long id, OrganizationResourceRequest request) {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[UPDATE RESOURCE] orgId={} id={}", orgId, id);

        OrganizationResource entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        if (!entity.getOrganizationId().equals(orgId)) {
            throw new ServiceException(ErrorMessages.SH13);
        }

        entity.setTitle(request.getTitle());
        entity.setType(request.getType());
        entity.setSource(request.getSource());
        entity.setUrl(request.getUrl());

        repository.save(entity);

        return mapToResponse(entity);
    }

    public void delete(Long id) {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[DELETE RESOURCE] orgId={} id={}", orgId, id);

        OrganizationResource entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        if (!entity.getOrganizationId().equals(orgId)) {
            throw new ServiceException(ErrorMessages.SH13);
        }

        repository.delete(entity);
    }

    public OrganizationResourceResponse get(Long id) {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[GET RESOURCE] orgId={} id={}", orgId, id);

        OrganizationResource entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        if (!entity.getOrganizationId().equals(orgId)) {
            throw new ServiceException(ErrorMessages.SH13);
        }

        return mapToResponse(entity);
    }


    private OrganizationResourceResponse mapToResponse(OrganizationResource entity) {
        return OrganizationResourceResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .type(entity.getType())
                .source(entity.getSource())
                .url(entity.getUrl())
                .build();
    }

    private SharkdomPaginatedResponse<OrganizationResourceResponse> mapToPaginatedResponse(Page<OrganizationResource> page) {

        SharkdomPaginatedResponse<OrganizationResourceResponse> response = new SharkdomPaginatedResponse<>();

        response.setContent(page.getContent().stream().map(this::mapToResponse).toList());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());

        return response;
    }

    public List<OrganizationResourceResponse> getByOrgId(Long orgId) {

        log.info("[GET RESOURCES BY ORG] orgId={}", orgId);

        List<OrganizationResource> resources = repository.findByOrganizationId(orgId);

        return resources.stream()
                .map(this::mapToResponse)
                .toList();
    }
}