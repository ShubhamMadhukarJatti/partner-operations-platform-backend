package com.sharkdom.model.ai;

import lombok.Data;

@Data
public class OverlapFieldRequest {

    private String name;

    private String companyName;

    private String contactEmail;

    private String domain;

    private String dealStage;

    private String creationDate;

    private String closeDate;

    private String subscribed;

    private String ticketSize;

    // ---------------- New Company Fields ----------------
    private String website;
    private String industry;
    private String companySize;
    private String country;
    private String linkedinUrl;
    private String annualRevenue;
    private String description;
    private String companyPhone;
    private String city;

    // ---------------- New Contact Fields ----------------
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String contactLinkedinUrl;
    private String leadStatus;
    private String contactPhone;
    private String lastActivityDate;
    private String contactOwner;
    private String associatedCompanyId;

    // ---------------- New Deal Fields ----------------
    private String dealName;
    private String dealOwner;
    private String amountAcv;
    private String hubspotDealId;
    private String pipeline;
    private String dealType;
    private String associatedContactId;

}
