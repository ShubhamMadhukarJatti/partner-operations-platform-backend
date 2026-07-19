package com.sharkdom.partnerattribution.service;

import com.sharkdom.partnerattribution.dto.SharedAssetRequestDTO;
import com.sharkdom.partnerattribution.dto.SharedAssetResponseDTO;
import com.sharkdom.partnerattribution.entities.SharedAsset;
import com.sharkdom.partnerattribution.repository.SharedAssetRepository;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedAssetService {

    private final SharedAssetRepository repository;

    // CREATE
    public SharedAssetResponseDTO create(SharedAssetRequestDTO request) {

        Long orgId = Util.getOrgIdFromToken();

        log.info("Creating Shared Asset | orgId={} | partnerOrgId={} | title={}",
                orgId, request.getPartnerOrgId(), request.getTitle());

        SharedAsset asset = new SharedAsset();
        asset.setOrgId(orgId);
        asset.setPartnerOrgId(request.getPartnerOrgId());
        asset.setTitle(request.getTitle());
        asset.setFileUrl(request.getFileUrl());
        asset.setDealId(request.getDealId());
        asset.setSharedBy(request.getUsername());

        SharedAsset saved = repository.save(asset);

        return mapToDto(saved);
    }

    // GET
    public List<SharedAssetResponseDTO> getAssets(Long partnerOrgId,String dealId) {

        Long orgId = Util.getOrgIdFromToken();

        log.info("Fetching Shared Assets | orgId={} | partnerOrgId={}",
                orgId, partnerOrgId);

        return repository
                .findByOrgIdAndPartnerOrgIdAndDealIdAndIsDeletedFalse(orgId, partnerOrgId,dealId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private SharedAssetResponseDTO mapToDto(SharedAsset entity) {

        SharedAssetResponseDTO dto = new SharedAssetResponseDTO();

        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setFileUrl(entity.getFileUrl());
        dto.setSharedBy(entity.getSharedBy());
        dto.setDealId(entity.getDealId());

        return dto;
    }
}
