package com.sharkdom.deals.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CommissionRequestResponse {

    private Long id;
    private Long organizationId;
    private Long requestingOrganizationId;
    private Long requestingOrganizationName;
    private String status;
    private Double amount;
    private LocalDate date;
    private String rejectingReason;
    private String name;
    private String invoiceAzure;
}
