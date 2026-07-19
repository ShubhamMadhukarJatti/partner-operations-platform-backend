package com.sharkdom.entity.stripe;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "stripe_bank_account_payout")
public class StripePayOutBankAccount extends BaseEntity {


    private String bankAccount_id; // Stripe's bank account ID, e.g., ba_1Rhiz...
    @Column(nullable = false)
    private String account;
    private String accountHolderName;
    private String accountHolderType;
    private String bankName;
    private String country;
    private String currency;
    private String last4;
    private String fingerprint;
    @Column(nullable = false)
    private String routingNumber;
    private String status;

    @ElementCollection
    private List<String> availablePayoutMethods;



}
