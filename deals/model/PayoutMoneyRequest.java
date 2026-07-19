package com.sharkdom.deals.model;

public record PayoutMoneyRequest(Long organizationId,
                                 String userId,
                                 Long receiverOrganizationId,
                                 Double amount) {
}
