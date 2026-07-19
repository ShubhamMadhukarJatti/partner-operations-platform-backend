package com.sharkdom.partnerattribution.service;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.ai.OverlapRecordFieldEntity;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.partnerattribution.dto.DealOwnerDetailsResponseDto;
import com.sharkdom.partnerattribution.dto.HubSpotOwnerResponseDto;
import com.sharkdom.partnerattribution.dto.PartnershipResponseMonitorDto;
import com.sharkdom.partnerattribution.hubspot.HubSpotOwnerService;
import com.sharkdom.repository.ai.OverlapRecordsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealOwnerDetailsService {

    private final OverlapRecordsRepository overlapRecordFieldRepository;

    private final HubSpotOwnerService hubSpotOwnerService;

    @Transactional(readOnly = true)
    public DealOwnerDetailsResponseDto getDealOwnerDetails(
            Long organizationId,
            String dealId
    ) {

        log.info(
                "Started fetching deal owner details. organizationId: {}, dealId: {}",
                organizationId,
                dealId
        );

        validateRequest(
                organizationId,
                dealId
        );

        OverlapRecordFieldEntity overlapRecord =
                fetchOverlapRecord(dealId);

        HubSpotOwnerResponseDto ownerResponse =
                fetchOwnerDetails(
                        organizationId,
                        overlapRecord
                );

        DealOwnerDetailsResponseDto response =
                mapResponse(
                        overlapRecord,
                        ownerResponse
                );

        log.info(
                "Successfully fetched deal owner details. organizationId: {}, dealId: {}",
                organizationId,
                dealId
        );

        return response;
    }

    private void validateRequest(
            Long organizationId,
            String dealId
    ) {

        log.debug(
                "Validating request for deal owner details. organizationId: {}, dealId: {}",
                organizationId,
                dealId
        );

        if (organizationId == null) {

            log.error(
                    "OrganizationId is null"
            );

            throw new SharkdomException(
                    ErrorMessages.SH199
            );
        }

        if (!StringUtils.hasText(dealId)) {

            log.error(
                    "DealId is null or empty"
            );

            throw new SharkdomException(
                    ErrorMessages.SH106
            );
        }
    }

    private OverlapRecordFieldEntity fetchOverlapRecord(
            String dealId
    ) {

        log.info(
                "Fetching overlap record using dealId: {}",
                dealId
        );

        OverlapRecordFieldEntity overlapRecord =
                overlapRecordFieldRepository
                        .findOverlapRecordFieldByDealId(dealId)
                        .orElseThrow(() -> {

                            log.error(
                                    "OverlapRecordFieldEntity not found for dealId: {}",
                                    dealId
                            );

                            return new SharkdomException(
                                    ErrorMessages.SH106
                            );
                        });

        log.info(
                "Successfully fetched overlap record for dealId: {}",
                dealId
        );

        return overlapRecord;
    }

    private HubSpotOwnerResponseDto fetchOwnerDetails(
            Long organizationId,
            OverlapRecordFieldEntity overlapRecord
    ) {

        log.info(
                "Started fetching HubSpot owner details for organizationId: {}",
                organizationId
        );

        if (!StringUtils.hasText(overlapRecord.getDealOwner())) {

            log.warn(
                    "Deal owner is null or empty for dealId: {}",
                    overlapRecord.getDealId()
            );

            return null;
        }

        HubSpotOwnerResponseDto ownerResponse =
                hubSpotOwnerService.getOwnerById(
                        organizationId,
                        overlapRecord.getDealOwner()
                );

        log.info(
                "Successfully fetched HubSpot owner details for ownerId: {}",
                overlapRecord.getDealOwner()
        );

        return ownerResponse;
    }

    private DealOwnerDetailsResponseDto mapResponse(
            OverlapRecordFieldEntity entity,
            HubSpotOwnerResponseDto ownerResponse
    ) {

        log.debug(
                "Started mapping deal owner response for dealId: {}",
                entity.getDealId()
        );

        DealOwnerDetailsResponseDto response =
                DealOwnerDetailsResponseDto
                        .builder()

                        // ---------------- Deal Details ----------------
                        .dealId(entity.getDealId())
                        .dealName(entity.getDealName())
                        .dealOwner(entity.getDealOwner())
                        .amountAcv(entity.getAmountAcv())
                        .dealStage(entity.getDealStage())
                        .pipeline(entity.getPipeline())
                        .dealType(entity.getDealType())
                        .creationDate(entity.getCreationDate())
                        .closeDate(entity.getCloseDate())

                        // ---------------- Company Details ----------------
                        .companyName(entity.getCompanyName())
                        .domain(entity.getDomain())
                        .website(entity.getWebsite())
                        .industry(entity.getIndustry())
                        .companySize(entity.getCompanySize())
                        .annualRevenue(entity.getAnnualRevenue())
                        .country(entity.getCountry())
                        .city(entity.getCity())
                        .companyPhone(entity.getCompanyPhone())
                        .linkedinUrl(entity.getLinkedinUrl())
                        .description(entity.getDescription())

                        // ---------------- Contact Details ----------------
                        .firstName(entity.getFirstName())
                        .lastName(entity.getLastName())
                        .contactEmail(entity.getContactEmail())
                        .jobTitle(entity.getJobTitle())
                        .contactPhone(entity.getContactPhone())
                        .contactLinkedinUrl(entity.getContactLinkedinUrl())
                        .leadStatus(entity.getLeadStatus())
                        .contactOwner(entity.getContactOwner())
                        .lastActivityDate(entity.getLastActivityDate())

                        // ---------------- Owner Details ----------------
                        .owner(ownerResponse)

                        .build();

        log.debug(
                "Successfully mapped response for dealId: {}",
                entity.getDealId()
        );

        return response;
    }

    public PartnershipResponseMonitorDto getStaticPartnershipTracker() {

        return PartnershipResponseMonitorDto.builder()
                .currentStage("Intro email sent")
                .stages(Arrays.asList(

                        PartnershipResponseMonitorDto.StageDto.builder()
                                .stageName("Request sent")
                                .completed(true)
                                .active(false)
                                .build(),

                        PartnershipResponseMonitorDto.StageDto.builder()
                                .stageName("Partner accepted")
                                .completed(true)
                                .active(false)
                                .build(),

                        PartnershipResponseMonitorDto.StageDto.builder()
                                .stageName("Intro email sent")
                                .completed(false)
                                .active(true)
                                .build(),

                        PartnershipResponseMonitorDto.StageDto.builder()
                                .stageName("First conversation")
                                .completed(false)
                                .active(false)
                                .build()
                ))
                .responseMonitor(

                        PartnershipResponseMonitorDto.ResponseMonitorDto.builder()
                                .emailDelivered(true)
                                .replyReceived(true)
                                .emailOpened(true)
                                .emailOpenedAt("10th May, 10:49 AM")
                                .responseDeadline("14th May, 2026")
                                .timeRemaining("1 Day, 12 hrs left")
                                .lastChecked("2 min ago")
                                .build()
                )
                .build();
    }
}