package com.sharkdom.partnerattribution.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutreachResponse {

    private String channel;
    private String recipientName;
    private String recipientTitle;
    private String recipientLinkedin;
    private String company;

    private String subject;
    private String message;

    private Integer wordCount;
    private Integer subjectLength;

    private String tone;
    private String personalisationSignal;
    private String complianceMaturityTier;
    private String primarySignalUsed;

    private Boolean guardrailTriggered;
    private String guardrailReason;

    private String whyItWorks;
    private String generatedAt;
    private String cooldownUntil;
}