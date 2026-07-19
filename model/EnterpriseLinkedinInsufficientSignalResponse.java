package com.sharkdom.agenticai.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class EnterpriseLinkedinInsufficientSignalResponse {

    private String channel;

    @JsonProperty("recipient_name")
    private String recipientName;

    @JsonProperty("recipient_title")
    private String recipientTitle;

    private String company;

    @JsonProperty("guardrail_triggered")
    private Boolean guardrailTriggered;

    @JsonProperty("guardrail_reason")
    private String guardrailReason;

    @JsonProperty("generated_at")
    private Instant generatedAt;

    @JsonProperty("cooldown_until")
    private Instant cooldownUntil;
}