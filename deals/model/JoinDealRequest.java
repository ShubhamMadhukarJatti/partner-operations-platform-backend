package com.sharkdom.deals.model;

public record JoinDealRequest(Long organizationId,
                              String dealId,
                              String userId) {
}
