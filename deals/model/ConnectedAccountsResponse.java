package com.sharkdom.deals.model;

public record ConnectedAccountsResponse(boolean stripeConnected, boolean bankConnected, boolean razorPayConnected) {
}
