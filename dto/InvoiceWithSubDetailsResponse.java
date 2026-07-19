package com.sharkdom.dto;

import com.sharkdom.entity.stripe.StripeInvoice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceWithSubDetailsResponse {
    private StripeInvoice invoice;
    private Long amount;
    private OffsetDateTime createdAt;
}