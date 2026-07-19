package com.sharkdom.reseller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResellerDealStatusCountDto {
    private String status;
    private Long count;
}