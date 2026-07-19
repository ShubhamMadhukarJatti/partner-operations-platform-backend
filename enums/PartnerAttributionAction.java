package com.sharkdom.partnerattribution.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartnerAttributionAction {

    MONITOR("Monitor"),
    ADD_TO_PIPELINE("Add to Pipeline"),
    REQUEST_INTRO("Request Intro"),
    START_CO_SELL("Start Co-sell"),
    NO_ACTION("No Action"),
    CO_SELL_ON_EXPANSION("Co-sell on Expansion"),
    JOINT_CUSTOMER("Joint Customer");

    private final String label;
}
