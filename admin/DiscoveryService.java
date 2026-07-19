package com.sharkdom.service.admin;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.admin.DiscoveryEntity;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.model.admin.DiscoveryEntityResponse;
import com.sharkdom.repository.admin.DiscoveryRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class DiscoveryService {

    private final DiscoveryRepository discoveryRepository;
    private final OrganizationRepository organizationRepository;

    public DiscoveryService(DiscoveryRepository discoveryRepository, OrganizationRepository organizationRepository) {
        this.discoveryRepository = discoveryRepository;
        this.organizationRepository = organizationRepository;
    }

    public DiscoveryEntity add(DiscoveryEntity discoveryEntity) {
        if (discoveryRepository.existsByOrganizationName(discoveryEntity.getOrganizationName())) {
            throw new SharkdomException(ErrorMessages.SH90);
        }
        if (organizationRepository.existsOrganizationByName(discoveryEntity.getOrganizationName())) {
            throw new SharkdomException(ErrorMessages.SH90);
        }
        return discoveryRepository.save(discoveryEntity);
    }

    public Page<DiscoveryEntityResponse> search(String partialName, int page, int size) {
        return discoveryRepository.searchOrganization(partialName, PageRequest.of(page, size));
    }
}

