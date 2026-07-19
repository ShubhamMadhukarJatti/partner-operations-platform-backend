package com.sharkdom.gtm.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Status {
    @JsonProperty("Not Started")
    NOT_STARTED,

    @JsonProperty("In Progress")
    IN_PROGRESS,

    @JsonProperty("On Track")
    ON_TRACK,

    @JsonProperty("At Risk")
    AT_RISK,

    @JsonProperty("Delayed")
    DELAYED,

    @JsonProperty("Paused")
    PAUSED,

    @JsonProperty("Blocked")
    BLOCKED,

    @JsonProperty("Completed")
    COMPLETED,

    @JsonProperty("Cancelled")
    CANCELLED
}
