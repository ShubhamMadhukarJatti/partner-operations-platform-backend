package com.sharkdom.partnerattribution.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.partnerattribution.dto.SharedContactRequestDTO;
import com.sharkdom.partnerattribution.dto.SharedContactResponseDTO;
import com.sharkdom.partnerattribution.entities.SharedContact;
import com.sharkdom.partnerattribution.repository.SharedContactRepository;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedContactService {

    private final SharedContactRepository repository;

    // CREATE
    public SharedContactResponseDTO create(SharedContactRequestDTO request) {

        Long orgId = Util.getOrgIdFromToken();

        log.info("Creating SharedContact | orgId={} | name={}", orgId, request.getName());

        SharedContact entity = new SharedContact();

        entity.setOrgId(orgId);
        entity.setPartnerOrgId(request.getPartnerOrgId());
        entity.setName(request.getName());
        entity.setTitle(request.getTitle());
        entity.setSource(request.getSource());
        entity.setRelationship(request.getRelationship());
        entity.setInCrm(request.getInCrm());
        entity.setDealId(request.getDealId());


        return map(repository.save(entity));
    }

    // UPDATE
    public SharedContactResponseDTO update(Long id, SharedContactRequestDTO request) {

        log.info("Updating SharedContact | id={}", id);

        SharedContact entity = repository.findById(id)
                .orElseThrow(() -> new SharkdomException(ErrorMessages.NOT_FOUND));

        entity.setName(request.getName());
        entity.setTitle(request.getTitle());
        entity.setSource(request.getSource());
        entity.setRelationship(request.getRelationship());
        entity.setInCrm(request.getInCrm());
        entity.setDealId(request.getDealId());

        return map(repository.save(entity));
    }

    // GET
    public List<SharedContactResponseDTO> get(Long partnerOrgId,String dealId) {

        Long orgId = Util.getOrgIdFromToken();

        log.info("Fetching SharedContacts | orgId={} | partnerOrgId={}",
                orgId, partnerOrgId);

        return repository
                .findByOrgIdAndPartnerOrgIdAndDealIdAndIsDeletedFalse(orgId, partnerOrgId,dealId)
                .stream()
                .map(this::map)
                .toList();
    }

    public void delete(Long id) {

        log.info("Deleting SharedContact | id={}", id);

        SharedContact entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("SharedContact not found | id={}", id);
                    return new SharkdomException(ErrorMessages.NOT_FOUND);
                });

        entity.setIsDeleted(true);

        repository.save(entity);

        log.info("SharedContact soft deleted successfully | id={}", id);
    }

    public List<SharedContactResponseDTO> getByDealId(Long partnerOrgId, String dealId) {

        Long orgId = Util.getOrgIdFromToken();

        log.info("Fetching SharedContacts by dealId | orgId={} | partnerOrgId={} | dealId={}",
                orgId, partnerOrgId, dealId);

        List<SharedContact> contacts =
                repository.findByOrgIdAndPartnerOrgIdAndDealIdAndIsDeletedFalse(
                        orgId, partnerOrgId, dealId
                );

        return contacts.stream()
                .map(this::map)
                .toList();
    }

    private SharedContactResponseDTO map(SharedContact e) {
        SharedContactResponseDTO dto = new SharedContactResponseDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setTitle(e.getTitle());
        dto.setDealId(e.getDealId());
        dto.setSource(e.getSource());
        dto.setRelationship(e.getRelationship());
        dto.setInCrm(e.getInCrm());
        return dto;
    }
}
