package com.sharkdom.deals.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AddMoneyResponse {
    private String transactionId;
    private double amount;
    private String type;
    private double availableAmount;
    private double totalAmount;
}
