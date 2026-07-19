package com.sharkdom.deals.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommissionRequestRequest {

    private Long orgId;
    private String InvoiceAzure;
    private Long requestingOrganizationId;
    private String requestingOrganizationName;
    private String transactionId;
    private Integer commission;

}
