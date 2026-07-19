package com.sharkdom.deals.model;

public record AddMoneyRequest(Long organizationId,
                              String userId,
                              String currency,
                              String orderId,
                              String paymentId,
                              Double amount) {
}
