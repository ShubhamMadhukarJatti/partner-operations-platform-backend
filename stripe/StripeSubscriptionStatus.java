package com.sharkdom.constants.stripe;

import lombok.Getter;

@Getter
public enum StripeSubscriptionStatus {

    INCOMPLETE("INCOMPLETE"),
    INCOMPLETE_EXPIRED("INCOMPLETE_EXPIRED"),
    TRIALING("TRIALING"),
    ACTIVE("ACTIVE"),
    PAST_DUE("PAST_DUE"),
    CANCELED("CANCELED"),
    UNPAID("UNPAID"),
    PAUSED("PAUSED"),
    PENDING_DOWNGRADE("PENDING_DOWNGRADE");

    private final String subscriptionStatus;

    StripeSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }
}
