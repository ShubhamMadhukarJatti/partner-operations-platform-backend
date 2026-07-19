package com.sharkdom.model.stripe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubscriptionRequest {

    private String customerId;

    private List<String> productIds;

    private String interval;

    private Long amount;

    private String currency;

    private Long trialPeriodDays;
}
