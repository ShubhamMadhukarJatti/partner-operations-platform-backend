package com.sharkdom.partnerattribution.service;

import com.sharkdom.partnerattribution.dto.AgreedNextStepRequestDto;
import com.sharkdom.partnerattribution.dto.AgreedNextStepResponseDto;
import com.sharkdom.partnerattribution.entities.AgreedNextStep;

public class AgreedNextStepMapper {

    public static AgreedNextStep toEntity(AgreedNextStepRequestDto dto) {
        AgreedNextStep entity = new AgreedNextStep();
        entity.setOrgId(dto.getOrgId());
        entity.setDealId(dto.getDealId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setOwner(dto.getOwner());
        entity.setPriority(dto.getPriority());
        entity.setDueDate(dto.getDueDate());
        entity.setIsCompleted(dto.getIsCompleted() != null ? dto.getIsCompleted() : false);
        return entity;
    }

    public static AgreedNextStepResponseDto toDto(AgreedNextStep entity) {
        AgreedNextStepResponseDto dto = new AgreedNextStepResponseDto();
        dto.setId(entity.getId());
        dto.setOrgId(entity.getOrgId());
        dto.setDealId(entity.getDealId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setOwner(entity.getOwner());
        dto.setPriority(entity.getPriority());
        dto.setDueDate(entity.getDueDate());
        dto.setIsCompleted(entity.getIsCompleted());
        return dto;
    }
}