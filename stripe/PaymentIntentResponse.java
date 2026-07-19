package com.sharkdom.model.stripe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PaymentIntentResponse {

    public String id;
    public String object;
    public Integer amount;

    @JsonProperty("amount_capturable")
    public Integer amountCapturable;

    @JsonProperty("amount_details")
    public AmountDetails amountDetails;

    @JsonProperty("amount_received")
    public Integer amountReceived;

    public String application;

    @JsonProperty("application_fee_amount")
    public Integer applicationFeeAmount;

    @JsonProperty("automatic_payment_methods")
    public Object automaticPaymentMethods;

    @JsonProperty("canceled_at")
    public Long canceledAt;

    @JsonProperty("cancellation_reason")
    public String cancellationReason;

    @JsonProperty("capture_method")
    public String captureMethod;

    @JsonProperty("client_secret")
    public String clientSecret;

    @JsonProperty("confirmation_method")
    public String confirmationMethod;

    public Long created;
    public String currency;
    public String customer;
    public String description;
    public String invoice;

    @JsonProperty("last_payment_error")
    public Object lastPaymentError;

    @JsonProperty("latest_charge")
    public String latestCharge;

    public Boolean livemode;
    public Map<String, Object> metadata;

    @JsonProperty("next_action")
    public Object nextAction;

    @JsonProperty("on_behalf_of")
    public String onBehalfOf;

    @JsonProperty("payment_method")
    public String paymentMethod;

    @JsonProperty("payment_method_configuration_details")
    public Object paymentMethodConfigurationDetails;

    @JsonProperty("payment_method_options")
    public PaymentMethodOptions paymentMethodOptions;

    @JsonProperty("payment_method_types")
    public List<String> paymentMethodTypes;

    public Object processing;

    @JsonProperty("receipt_email")
    public String receiptEmail;

    public String review;

    @JsonProperty("setup_future_usage")
    public String setupFutureUsage;

    public Object shipping;
    public String source;

    @JsonProperty("statement_descriptor")
    public String statementDescriptor;

    @JsonProperty("statement_descriptor_suffix")
    public String statementDescriptorSuffix;

    public String status;

    @JsonProperty("transfer_data")
    public Object transferData;

    @JsonProperty("transfer_group")
    public String transferGroup;

    public static class AmountDetails {
        public Map<String, Object> tip;
    }

    public static class PaymentMethodOptions {
        public Card card;

        public static class Card {
            public Object installments;

            @JsonProperty("mandate_options")
            public Object mandateOptions;

            public String network;

            @JsonProperty("request_three_d_secure")
            public String requestThreeDSecure;
        }
    }

}
