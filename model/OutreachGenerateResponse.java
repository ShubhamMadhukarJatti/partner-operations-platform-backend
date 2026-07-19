package com.sharkdom.agenticai.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class OutreachGenerateResponse {

    private String channel;

    @JsonProperty("recipient_name")
    private String recipientName;

    @JsonProperty("recipient_title")
    private String recipientTitle;

    @JsonProperty("recipient_linkedin")
    private String recipientLinkedin;

    private String company;

    private String subject;

    private String message;

    @JsonProperty("word_count")
    private Integer wordCount;

    @JsonProperty("subject_length")
    private Integer subjectLength;

    private String tone;

    @JsonProperty("personalisation_signal")
    private String personalisationSignal;

    @JsonProperty("compliance_maturity_tier")
    private String complianceMaturityTier;

    @JsonProperty("primary_signal_used")
    private String primarySignalUsed;

    @JsonProperty("forbidden_phrases_used")
    private List<String> forbiddenPhrasesUsed;

    @JsonProperty("guardrail_triggered")
    private Boolean guardrailTriggered;

    @JsonProperty("guardrail_reason")
    private String guardrailReason;

    @JsonProperty("why_it_works")
    private String whyItWorks;

    @JsonProperty("generated_at")
    private Instant generatedAt;

    @JsonProperty("cooldown_until")
    private Instant cooldownUntil;
}