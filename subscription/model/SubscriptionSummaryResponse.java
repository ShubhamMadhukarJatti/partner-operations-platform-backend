package com.sharkdom.subscription.model;

import com.sharkdom.reseller.entity.PaymentStatus;
import com.sharkdom.subscription.entity.SuiteKey;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SubscriptionSummaryResponse {

    private PaymentStatus status;

    private Long daysRemaining;

    private Boolean inGrace;

    private LocalDateTime graceEndsAt;

    private List<SuiteKey> activeSuites;
}