package com.sharkdom.deals.model;

public record StripeConnectRequest(String userId,
                                   Long organizationId,
                                   String connectedId,
                                   String publishableKey,
                                   String refreshToken) {
}
