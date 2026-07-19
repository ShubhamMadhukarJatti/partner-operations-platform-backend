package com.sharkdom.model.stripe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionUpgradeResponse {

    public enum Status {
        SUCCESS,
        REQUIRES_PAYMENT,
        FAILED
    }

    private Status status;
    private String message;
    private String subscriptionId;
    private String invoiceId;
    private String paymentIntentClientSecret;
    private Long amountDue;
    private String currency;
    private String hostedInvoiceUrl;

    // Static factory methods
    public static SubscriptionUpgradeResponse success(String subscriptionId, String invoiceId, String message) {
        return SubscriptionUpgradeResponse.builder()
                .subscriptionId(subscriptionId)
                .invoiceId(invoiceId)
                .message(message)
                .build();
    }

    public static SubscriptionUpgradeResponse requiresPayment(String subscriptionId, String invoiceId, String clientSecret, Long amountDue, String currency, String hostedInvoiceUrl) {
        return SubscriptionUpgradeResponse.builder()
                .subscriptionId(subscriptionId)
                .invoiceId(invoiceId)
                .paymentIntentClientSecret(clientSecret)
                .amountDue(amountDue)
                .currency(currency)
                .hostedInvoiceUrl(hostedInvoiceUrl)
                .build();
    }

    public static SubscriptionUpgradeResponse failure(String message) {
        return SubscriptionUpgradeResponse.builder()
                .message(message)
                .build();
    }

}
