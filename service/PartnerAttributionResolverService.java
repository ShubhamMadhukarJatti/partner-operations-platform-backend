package com.sharkdom.partnerattribution.service;

import com.sharkdom.model.ai.RecordType;
import com.sharkdom.partnerattribution.enums.CoSellMotion;
import com.sharkdom.partnerattribution.enums.PartnerAttributionAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PartnerAttributionResolverService {

    public PartnerAttributionResponse resolve(
            RecordType sourceType,
            RecordType targetType
    ) {
        log.info(
                "Resolving partner attribution. sourceType={}, targetType={}",
                sourceType,
                targetType
        );

        // CONTACT -> CONTACT
        if (sourceType == RecordType.CONTACTS &&
                targetType == RecordType.CONTACTS) {
            return build(
                    PartnerAttributionAction.MONITOR,
                    CoSellMotion.LOW_PRIORITY_ENGAGEMENT
            );
        }

        // CONTACT -> DEAL
        if (sourceType == RecordType.CONTACTS &&
                targetType == RecordType.DEALS) {
            return build(
                    PartnerAttributionAction.ADD_TO_PIPELINE,
                    CoSellMotion.PARTNER_VALIDATION
            );
        }

        // CONTACT -> COMPANY
        if (sourceType == RecordType.CONTACTS &&
                targetType == RecordType.COMPANIES) {
            return build(
                    PartnerAttributionAction.REQUEST_INTRO,
                    CoSellMotion.WARM_DOOR_ACCESS
            );
        }

        // DEAL -> CONTACT
        if (sourceType == RecordType.DEALS &&
                targetType == RecordType.CONTACTS) {
            return build(
                    PartnerAttributionAction.ADD_TO_PIPELINE,
                    CoSellMotion.ESCALATE_MQL
            );
        }

        // DEAL -> DEAL
        if (sourceType == RecordType.DEALS &&
                targetType == RecordType.DEALS) {
            return build(
                    PartnerAttributionAction.START_CO_SELL,
                    CoSellMotion.JOINT_MOTION
            );
        }

        // DEAL -> COMPANY
        if (sourceType == RecordType.DEALS &&
                targetType == RecordType.COMPANIES) {
            return build(
                    PartnerAttributionAction.START_CO_SELL,
                    CoSellMotion.PARTNER_SUPPORT
            );
        }

        // COMPANY -> CONTACT
        if (sourceType == RecordType.COMPANIES &&
                targetType == RecordType.CONTACTS) {
            return build(
                    PartnerAttributionAction.NO_ACTION,
                    CoSellMotion.ALREADY_WON
            );
        }

        // COMPANY -> DEAL
        if (sourceType == RecordType.COMPANIES &&
                targetType == RecordType.DEALS) {
            return build(
                    PartnerAttributionAction.CO_SELL_ON_EXPANSION,
                    CoSellMotion.PURSUIT
            );
        }

        // COMPANY -> COMPANY
        if (sourceType == RecordType.COMPANIES &&
                targetType == RecordType.COMPANIES) {
            return build(
                    PartnerAttributionAction.JOINT_CUSTOMER,
                    CoSellMotion.ADVOCACY_CASE_STUDY
            );
        }

        throw new IllegalArgumentException(
                "Unsupported combination: "
                        + sourceType + " -> " + targetType
        );
    }

    private PartnerAttributionResponse build(
            PartnerAttributionAction action,
            CoSellMotion motion
    ) {
        return PartnerAttributionResponse.builder()
                .action(action)
                .motion(motion)
                .build();
    }
}
