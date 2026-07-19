package com.sharkdom.partnerattribution.service;

import com.sharkdom.partnerattribution.enums.CoSellMotion;
import com.sharkdom.partnerattribution.enums.PartnerAttributionAction;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerAttributionResponse {

    private PartnerAttributionAction action;
    private CoSellMotion motion;
}
