package com.sharkdom.partnerprogram.service.impl;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnerprogram.dtos.PartnerCommissionDTO;
import com.sharkdom.partnerprogram.dtos.PartnerCommissionStatsDTO;
import com.sharkdom.partnerprogram.dtos.PartnerResourceDTO;
import com.sharkdom.partnerprogram.entities.PartnerResource;
import com.sharkdom.partnerprogram.repository.PartnerResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerResourceService {

    private final PartnerResourceRepository repository;

    public PartnerResourceDTO create(PartnerResourceDTO dto) {
        PartnerResource resource = PartnerResource.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .link(dto.getLink())
                .build();

        return mapToDTO(repository.save(resource));
    }

    public PartnerResourceDTO update(Long id, PartnerResourceDTO dto) {
        PartnerResource resource = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        resource.setTitle(dto.getTitle());
        resource.setDescription(dto.getDescription());
        resource.setLink(dto.getLink());

        return mapToDTO(repository.save(resource));
    }

    public void delete(Long id) {
        PartnerResource resource = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        repository.delete(resource);
    }

    public PartnerResourceDTO getById(Long id) {
        PartnerResource resource = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        return mapToDTO(resource);
    }

    public List<PartnerResourceDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private PartnerResourceDTO mapToDTO(PartnerResource resource) {
        return PartnerResourceDTO.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .link(resource.getLink())
                .created(resource.getCreationTimestamp())
                .updated(resource.getLastUpdatedTimestamp())
                .build();
    }
}