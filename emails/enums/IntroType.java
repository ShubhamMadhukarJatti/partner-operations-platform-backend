package com.sharkdom.partnerattribution.emails.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IntroType {

    VENDOR_TO_PARTNER("vendor-to-partner"),
    PARTNER_TO_TARGET("partner-to-target"),
    PARTNER_TO_TARGET_LINKEDIN("partner-to-target-linkedin"),
    VENDOR_TO_PARTNER_LINKEDIN("vendor-to-partner-linkedin"),
    PARTNER_TO_TARGET_MEETING("partner-to-target-meeting"),
    VENDOR_TO_PARTNER_MEETING("vendor-to-partner-meeting");

    private final String value;

    IntroType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static IntroType fromValue(String value) {
        for (IntroType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid intro type: " + value);
    }
}