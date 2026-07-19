package com.sharkdom.reseller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResellerLicenseDetailsDto {

    private Long licensesPurchased;
    private Long licensesAllocated;
    private Long licensesRemaining;
    private Long licensesConsumed;
}