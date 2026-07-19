package com.sharkdom.partnerattribution.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OverlapType {

    HOT_OVERLAP("Hot Overlap"),
    CO_SELL_READY("Co-sell Ready"),
    MONITOR("Monitor"),
    LOW_PRIORITY("Low Priority");

    private final String label;
}