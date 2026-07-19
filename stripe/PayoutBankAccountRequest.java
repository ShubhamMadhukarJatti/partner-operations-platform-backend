package com.sharkdom.model.stripe;

import lombok.Data;

@Data
public class PayoutBankAccountRequest {
    private String country;
    private String currency;
    private String accountHolderName;
    private String accountHolderType;
    private String accountNumber;
    private String routingNumber;
}
