package com.sharkdom.partnerattribution.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.partnerattribution.dto.AgreedNextStepRequestDto;
import com.sharkdom.partnerattribution.dto.AgreedNextStepResponseDto;
import com.sharkdom.partnerattribution.entities.AgreedNextStep;
import com.sharkdom.partnerattribution.repository.AgreedNextStepRepository;
import com.sharkdom.util.SharkdomPaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgreedNextStepService {

    private final AgreedNextStepRepository repository;

    // CREATE
    public AgreedNextStepResponseDto create(AgreedNextStepRequestDto request) {
        log.info("Creating AgreedNextStep | orgId={} | title={}", request.getOrgId(), request.getTitle());

        AgreedNextStep entity = AgreedNextStepMapper.toEntity(request);
        AgreedNextStep saved = repository.save(entity);

        log.info("AgreedNextStep created successfully | id={}", saved.getId());

        return AgreedNextStepMapper.toDto(saved);
    }

    // UPDATE
    public AgreedNextStepResponseDto update(Long id, AgreedNextStepRequestDto request) {
        log.info("Updating AgreedNextStep | id={}", id);

        AgreedNextStep existing = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("AgreedNextStep not found | id={}", id);
                    return new SharkdomException(ErrorMessages.NOT_FOUND);
                });

        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setOwner(request.getOwner());
        existing.setPriority(request.getPriority());
        existing.setDueDate(request.getDueDate());
        existing.setIsCompleted(request.getIsCompleted());
        existing.setDealId(request.getDealId());

        AgreedNextStep updated = repository.save(existing);

        log.info("AgreedNextStep updated successfully | id={}", id);

        return AgreedNextStepMapper.toDto(updated);
    }

    // DELETE (Soft)
    public void delete(Long id) {
        log.info("Deleting AgreedNextStep | id={}", id);

        AgreedNextStep existing = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("AgreedNextStep not found for delete | id={}", id);
                    return new SharkdomException(ErrorMessages.NOT_FOUND);
                });

        existing.setIsDeleted(true);
        repository.save(existing);

        log.info("AgreedNextStep soft deleted | id={}", id);
    }

    // GET BY ORG ID
    public SharkdomPaginatedResponse<AgreedNextStepResponseDto> getByOrgId(Long orgId,String dealId, int page, int size) {

        log.info("Fetching AgreedNextSteps | orgId={} | page={} | size={}", orgId, page, size);

        Page<AgreedNextStep> result =
                repository.findByOrgIdAndDealIdAndIsDeletedFalse(orgId, dealId,PageRequest.of(page, size));

        SharkdomPaginatedResponse<AgreedNextStepResponseDto> response =
                new SharkdomPaginatedResponse<>();

        response.setContent(
                result.getContent().stream()
                        .map(AgreedNextStepMapper::toDto)
                        .toList()
        );
        response.setPage(result.getNumber());
        response.setSize(result.getSize());
        response.setTotalElements(result.getTotalElements());
        response.setTotalPages(result.getTotalPages());
        response.setLast(result.isLast());

        log.info("Fetched {} AgreedNextSteps for orgId={}", result.getNumberOfElements(), orgId);

        return response;
    }
}