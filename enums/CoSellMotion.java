package com.sharkdom.partnerattribution.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CoSellMotion {

    LOW_PRIORITY_ENGAGEMENT("low-priority engagement"),
    PARTNER_VALIDATION("partner validation"),
    WARM_DOOR_ACCESS("warm door access"),
    ESCALATE_MQL("escalate MQL"),
    JOINT_MOTION("joint motion"),
    PARTNER_SUPPORT("partner support"),
    ALREADY_WON("already won"),
    PURSUIT("pursuit"),
    ADVOCACY_CASE_STUDY("advocacy/case study");

    private final String label;
}