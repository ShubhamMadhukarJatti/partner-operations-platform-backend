package com.sharkdom.model.stripe;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeInvoiceDto {

    @JsonBackReference
    private Long id;

    private String invoiceId;

    private String subscriptionId;

    private String customerId;

    private String invoicePdfUrl;

}
