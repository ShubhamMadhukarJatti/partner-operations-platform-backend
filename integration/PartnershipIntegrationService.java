package com.sharkdom.service.integration;

import com.sharkdom.entity.ai.ModeSaveEntity;
import com.sharkdom.entity.ai.Order;
import com.sharkdom.entity.integration.Endpoints;
import com.sharkdom.entity.integration.PartnershipIntegration;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.model.ai.ModeSaveRequest;
import com.sharkdom.model.ai.ModeSaveResponse;
import com.sharkdom.model.integration.PartnershipIntegrationRequest;
import com.sharkdom.repository.ai.ModeSaveRepository;
import com.sharkdom.repository.integration.PartnershipIntegrationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PartnershipIntegrationService {
    private final PartnershipIntegrationRepository partnershipIntegrationRepository;
    private final ModeSaveRepository modeSaveRepository;

    public PartnershipIntegrationService(PartnershipIntegrationRepository partnershipIntegrationRepository, ModeSaveRepository modeSaveRepository) {
        this.partnershipIntegrationRepository = partnershipIntegrationRepository;

        this.modeSaveRepository = modeSaveRepository;
    }

    public PartnershipIntegration saveIntegration(PartnershipIntegrationRequest partnershipIntegrationRequest) {
        var integration = partnershipIntegrationRepository.findByOrganizationId(partnershipIntegrationRequest.organizationId());
        if (integration != null) {
            if (Objects.nonNull(partnershipIntegrationRequest.category())) {
                integration.setCategory(partnershipIntegrationRequest.category());
            }
            if (Objects.nonNull(partnershipIntegrationRequest.sectorsAllowed())) {
                integration.setSectorsAllowed(partnershipIntegrationRequest.sectorsAllowed());
            }
            if (Objects.nonNull(partnershipIntegrationRequest.endpoints())) {
                var endpoints = partnershipIntegrationRequest.endpoints().stream()
                        .map(endpoint -> new Endpoints(endpoint.endpoint(), endpoint.method()))
                        .collect(Collectors.toList());
                integration.setEndpoints(endpoints);
            }
            if (Objects.nonNull(partnershipIntegrationRequest.docUrl())) {
                integration.setDocUrl(partnershipIntegrationRequest.docUrl());
            }
            if (Objects.nonNull(partnershipIntegrationRequest.integrationType())) {
                integration.setIntegrationType(partnershipIntegrationRequest.integrationType());
            }
            if (Objects.nonNull(partnershipIntegrationRequest.endpointUrl())) {
                integration.setEndpointUrl(partnershipIntegrationRequest.endpointUrl());
            }
            return partnershipIntegrationRepository.save(integration);
        }
        var endpoints = partnershipIntegrationRequest.endpoints().stream()
                .map(endpoint -> new Endpoints(endpoint.endpoint(), endpoint.method()))
                .toList();

        var pi = PartnershipIntegration.builder()
                .sectorsAllowed(partnershipIntegrationRequest.sectorsAllowed())
                .category(partnershipIntegrationRequest.category())
                .endpoints(endpoints)
                .integrationType(partnershipIntegrationRequest.integrationType())
                .organizationId(partnershipIntegrationRequest.organizationId())
                .endpointUrl(partnershipIntegrationRequest.endpointUrl())
                .docUrl(partnershipIntegrationRequest.docUrl()).build();
        return partnershipIntegrationRepository.save(pi);
    }

    public PartnershipIntegration updateIntegration(PartnershipIntegrationRequest partnershipIntegrationRequest) {
        var integration = partnershipIntegrationRepository.findByOrganizationId(partnershipIntegrationRequest.organizationId());
        if (Objects.nonNull(partnershipIntegrationRequest.category())) {
            integration.setCategory(partnershipIntegrationRequest.category());
        }
        if (Objects.nonNull(partnershipIntegrationRequest.sectorsAllowed())) {
            integration.setSectorsAllowed(partnershipIntegrationRequest.sectorsAllowed());
        }
        if (Objects.nonNull(partnershipIntegrationRequest.endpoints())) {
            var endpoints = partnershipIntegrationRequest.endpoints().stream()
                    .map(endpoint -> new Endpoints(endpoint.endpoint(), endpoint.method()))
                    .collect(Collectors.toList());
            integration.setEndpoints(endpoints);
        }
        if (Objects.nonNull(partnershipIntegrationRequest.docUrl())) {
            integration.setDocUrl(partnershipIntegrationRequest.docUrl());
        }

        return partnershipIntegrationRepository.save(integration);
    }

    public PartnershipIntegration getById(Long organizationId) {
        return partnershipIntegrationRepository.findByOrganizationId(organizationId);
    }

    public Map<String, String> saveModeDetails(ModeSaveRequest modeSaveRequest) {
        ModeSaveEntity modeSaveEntity =
                ModeSaveEntity.builder()
                        .organizationId(modeSaveRequest.organizationId())
                        .mode(modeSaveRequest.mode())
                        .entity("PARTNER_LISTING")
                        .build();
        modeSaveRepository.save(modeSaveEntity);
        return Map.of("response", "details saved successfully");
    }

    public Page<ModeSaveResponse> getEntities(int page, int size, Order order) {
        Sort sort = order.equals(Order.ASCENDING)
                ? Sort.by("creationTimestamp").ascending()
                : Sort.by("creationTimestamp").descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ModeSaveEntity> data = modeSaveRepository.findByEntity("PARTNER_LISTING", pageable);

        return data.map(modeSaveEntity -> {
            var integration = partnershipIntegrationRepository.findByOrganizationId(modeSaveEntity.getOrganizationId());
            var modeSaveResponse = ModeSaveResponse.builder();

            if (integration != null) {
                modeSaveResponse.createdOn(integration.getCreationTimestamp()).created(true);
            } else {
                modeSaveResponse.createdOn(null).created(false);
            }

            return modeSaveResponse
                    .mode(modeSaveEntity.getMode())
                    .organizationId(modeSaveEntity.getOrganizationId())
                    .clickedOn(modeSaveEntity.getCreationTimestamp())
                    .build();
        });
    }


}
