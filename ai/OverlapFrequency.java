package com.sharkdom.model.ai;

import java.time.Duration;

public enum OverlapFrequency {
    NONE(Duration.ZERO),

    FIFTEEN_MINUTES(Duration.ofMinutes(15)),

    WEEKLY(Duration.ofDays(7)),
    FIFTEEN_DAYS(Duration.ofDays(15)),
    THIRTY_DAYS(Duration.ofDays(30)),
    NINETY_DAYS(Duration.ofDays(90));

    private final Duration duration;

    OverlapFrequency(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }
}
