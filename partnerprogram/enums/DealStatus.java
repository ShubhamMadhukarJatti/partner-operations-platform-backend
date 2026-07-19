package com.sharkdom.partnerprogram.enums;

public enum DealStatus {

    DEMO_SCHEDULED("DEMO_SCHEDULED"),
    ACCEPTED("ACCEPTED"),
    DECLINED("DECLINED"),
    ONBOARDED("ONBOARDED"),
    UNDER_REVIEW("UNDER_REVIEW");

    private final String value;

    DealStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}