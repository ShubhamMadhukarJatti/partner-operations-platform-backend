package com.sharkdom.subscription.model;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponseDTO {

    private String invoiceId;

    private Long amount;

    private String currency;

    private String status;

    private LocalDateTime createdAt;

    private String hostedInvoiceUrl;

    private String invoicePdf;
}
