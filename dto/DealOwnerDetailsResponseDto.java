package com.sharkdom.partnerattribution.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DealOwnerDetailsResponseDto {

    // ---------------- Deal Details ----------------
    private String dealId;
    private String dealName;
    private String dealOwner;
    private String amountAcv;
    private String dealStage;
    private String pipeline;
    private String dealType;
    private String creationDate;
    private String closeDate;

    // ---------------- Company Details ----------------
    private String companyName;
    private String domain;
    private String website;
    private String industry;
    private String companySize;
    private String annualRevenue;
    private String country;
    private String city;
    private String companyPhone;
    private String linkedinUrl;
    private String description;

    // ---------------- Contact Details ----------------
    private String firstName;
    private String lastName;
    private String contactEmail;
    private String jobTitle;
    private String contactPhone;
    private String contactLinkedinUrl;
    private String leadStatus;
    private String contactOwner;
    private String lastActivityDate;

    // ---------------- Owner Details ----------------
    private HubSpotOwnerResponseDto owner;
}