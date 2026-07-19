package com.sharkdom.reseller.dto;

import lombok.Data;

@Data
public class PartnerTierCalculatedResponse {
    private String tierName;
    private String currency;
    private Double actualPrice;
    private Double buyPrice;
}
