package com.sharkdom.reseller.dto;

import lombok.Data;

@Data
public class ResellerDealCustomerRequest {
    private String email;
    private String customerName;
    private Long resellerDealId;
}