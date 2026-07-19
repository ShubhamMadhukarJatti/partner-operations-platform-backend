package com.sharkdom.dto;

import com.sharkdom.entity.stripe.StripeInvoice;
import com.sharkdom.entity.stripe.StripeSubscriptionData;
import lombok.Data;

import java.util.List;

@Data
public class StripeSubscriptionWithInvoiceResponse {

    private StripeSubscriptionData subscriptionData;

    private List<StripeInvoice> invoices;
}