package com.sharkdom.reseller.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "t_reseller_licenses")
public class ResellerDealLicense extends BaseEntity {

    @Column(name = "license_key", unique = true, nullable = false)
    private String licenseKey;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "reseller_deal_id")
    private Long dealId;

    @Column(name = "expiry_date")
    private Date expiryDate;

    @Enumerated(EnumType.STRING)
    private LicenseStatus status;
}