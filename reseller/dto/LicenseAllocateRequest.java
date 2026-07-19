package com.sharkdom.reseller.dto;

import lombok.Data;

@Data
public class LicenseAllocateRequest {
    private Long customerId;
    private Long dealId;
    private Integer validityInDays;
}
