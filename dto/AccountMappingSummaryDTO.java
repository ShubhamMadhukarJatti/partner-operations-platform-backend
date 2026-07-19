package com.sharkdom.partnerattribution.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class AccountMappingSummaryDTO {

    private Integer yourAccounts;
    private Integer partnerAccounts;
    private Integer sharedAccounts;

    private Integer onlyYou;
    private Integer onlyPartner;

    private OverlapCategoriesDTO overlapCategories;

    private Instant lastSynced;

}
