package com.sharkdom.partnerattribution.addtopipeline;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartnerDealServiceImpl implements PartnerDealService {

    private final PartnerDealRepository partnerDealRepository;

    @Override
    public PartnerDealResponseDto createDeal(
            PartnerDealRequestDto requestDto) {

        if (partnerDealRepository.existsByDealId(
                requestDto.getDealId())) {

            throw new ServiceException(
                    ErrorMessages.SH103,
                    requestDto.getDealId());
        }

        PartnerDeal partnerDeal = new PartnerDeal();

        mapToEntity(partnerDeal, requestDto);

        PartnerDeal savedDeal =
                partnerDealRepository.save(partnerDeal);

        return mapToResponse(savedDeal);
    }

    @Override
    public PartnerDealResponseDto updateDeal(
            Long id,
            PartnerDealRequestDto requestDto) {

        PartnerDeal partnerDeal =
                partnerDealRepository.findById(id)
                        .orElseThrow(() ->
                                new ServiceException(
                                        ErrorMessages.NOT_FOUND));

        mapToEntity(partnerDeal, requestDto);

        PartnerDeal updatedDeal =
                partnerDealRepository.save(partnerDeal);

        return mapToResponse(updatedDeal);
    }

    @Override
    public void deleteDeal(Long id) {

        PartnerDeal partnerDeal =
                partnerDealRepository.findById(id)
                        .orElseThrow(() ->
                                new ServiceException(
                                        ErrorMessages.NOT_FOUND));

        partnerDealRepository.delete(partnerDeal);
    }

    @Override
    public PartnerDealResponseDto getDealByDealId(
            String dealId) {

        PartnerDeal partnerDeal =
                partnerDealRepository.findByDealId(dealId)
                        .orElseThrow(() ->
                                new ServiceException(
                                        ErrorMessages.NOT_FOUND));

        return mapToResponse(partnerDeal);
    }

    private void mapToEntity(PartnerDeal partnerDeal,
                             PartnerDealRequestDto requestDto) {

        partnerDeal.setDealId(requestDto.getDealId());
        partnerDeal.setSalesTeamMemberId(
                requestDto.getSalesTeamMemberId());
        partnerDeal.setDealName(requestDto.getDealName());
        partnerDeal.setOrgId(requestDto.getOrgId());
        partnerDeal.setPartnerOrgId(
                requestDto.getPartnerOrgId());
        partnerDeal.setAccountName(
                requestDto.getAccountName());
        partnerDeal.setPipelineType(
                requestDto.getPipelineType());
        partnerDeal.setDealStage(
                requestDto.getDealStage());
        partnerDeal.setEstimatedAcv(
                requestDto.getEstimatedAcv());
        partnerDeal.setTargetCloseDate(
                requestDto.getTargetCloseDate());
        partnerDeal.setPriorityLevel(
                requestDto.getPriorityLevel());
        partnerDeal.setOpportunityScore(
                requestDto.getOpportunityScore());
        partnerDeal.setAeNotes(
                requestDto.getAeNotes());
        partnerDeal.setDealTags(
                requestDto.getDealTags());
    }

    private PartnerDealResponseDto mapToResponse(
            PartnerDeal partnerDeal) {

        return PartnerDealResponseDto.builder()
                .id(partnerDeal.getId())
                .dealId(partnerDeal.getDealId())
                .salesTeamMemberId(
                        partnerDeal.getSalesTeamMemberId())
                .orgId(partnerDeal.getOrgId())
                .partnerOrgId(
                        partnerDeal.getPartnerOrgId())
                .accountName(
                        partnerDeal.getAccountName())
                .pipelineType(
                        partnerDeal.getPipelineType())
                .dealStage(
                        partnerDeal.getDealStage())
                .estimatedAcv(
                        partnerDeal.getEstimatedAcv())
                .targetCloseDate(
                        partnerDeal.getTargetCloseDate())
                .priorityLevel(
                        partnerDeal.getPriorityLevel())
                .opportunityScore(
                        partnerDeal.getOpportunityScore())
                .aeNotes(
                        partnerDeal.getAeNotes())
                .dealTags(
                        partnerDeal.getDealTags())
                .dealName(partnerDeal.getDealName())
                .build();
    }
}