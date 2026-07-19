package com.sharkdom.reseller.dto;

import com.sharkdom.reseller.entity.LicenseStatus;
import lombok.Data;

import java.util.Date;

@Data
public class LicenseAllocateResponse {

    private Long customerId;
    private Long dealId;

    private String customerName;
    private String email;

    private String licenseKey;
    private LicenseStatus licenseStatus;

    private Date expiryDate;
    private Date allocatedOn;
}
