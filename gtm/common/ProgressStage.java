package com.sharkdom.gtm.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ProgressStage {
    @JsonProperty("Ideation")
    IDEATION,

    @JsonProperty("Partner Engaged")
    PARTNER_ENGAGED,

    @JsonProperty("Proposal Sent")
    PROPOSAL_SENT,

    @JsonProperty("In Execution")
    IN_EXECUTION,

    @JsonProperty("In Review")
    IN_REVIEW,

    @JsonProperty("Closed / Launched")
    CLOSED_LAUNCHED
}
