package com.sharkdom.reseller.dto;

import com.sharkdom.reseller.entity.LicenseStatus;
import lombok.Data;

import java.util.Date;

@Data
public class ResellerDealCustomerResponse {
    private Long dealId;
    private Long id;
    private Long resellerDealId;
    private Long customerId;
    private String email;
    private String customerName;

    // License related
    private boolean licenseAssigned;

    private String licenseKey;
    private LicenseStatus licenseStatus;
    private Date expiryDate;
    private Long expiryInDays;
}