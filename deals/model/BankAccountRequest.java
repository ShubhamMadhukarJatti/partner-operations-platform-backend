package com.sharkdom.deals.model;

public record BankAccountRequest(String holderName,
                                 String ifscCode,
                                 String accountNumber,
                                 Long organizationId,
                                 String userId) {
}
