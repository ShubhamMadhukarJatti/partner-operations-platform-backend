package com.sharkdom.partnerattribution.service;

import com.sharkdom.partnerattribution.dto.ActivityLogRequestDTO;
import com.sharkdom.partnerattribution.dto.ActivityLogResponseDTO;
import com.sharkdom.partnerattribution.entities.PartnerActivityLog;
import com.sharkdom.partnerattribution.repository.PartnerActivityLogRepository;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService {

    private final PartnerActivityLogRepository repository;

    // CREATE LOG
    public void createLog(ActivityLogRequestDTO request) {

        Long orgId = Util.getOrgIdFromToken();

        log.info("Creating Activity Log | orgId={} | partnerOrgId={} | title={}",
                orgId, request.getPartnerOrgId(), request.getTitle());

        PartnerActivityLog logEntity = new PartnerActivityLog();

        logEntity.setOrgId(orgId);
        logEntity.setPartnerOrgId(request.getPartnerOrgId());
        logEntity.setTitle(request.getTitle());
        logEntity.setDealId(request.getDealId());
        logEntity.setDescription(request.getDescription());
        logEntity.setActivityType(request.getActivityType());
        logEntity.setActor(request.getUserName());
        logEntity.setActivityDate(LocalDateTime.now());

        repository.save(logEntity);

        log.info("Activity Log created successfully");
    }

    // FETCH LOGS
    public List<ActivityLogResponseDTO> getLogs(Long partnerOrgId,String dealId) {

        Long orgId = Util.getOrgIdFromToken();

        log.info("Fetching Activity Logs | orgId={} | partnerOrgId={}",
                orgId, partnerOrgId);

        return repository
                .findByOrgIdAndPartnerOrgIdAndDealIdAndIsDeletedFalseOrderByActivityDateDesc(orgId, partnerOrgId,dealId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private ActivityLogResponseDTO mapToDto(PartnerActivityLog entity) {

        ActivityLogResponseDTO dto = new ActivityLogResponseDTO();

        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setActor(entity.getActor());
        dto.setType(entity.getActivityType());
        dto.setDate(formatDate(entity.getActivityDate()));
        dto.setDealId(entity.getDealId());

        return dto;
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime.toLocalDate().toString(); // customize UI format later
    }
}