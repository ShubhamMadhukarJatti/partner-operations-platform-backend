package com.sharkdom.service.partenerDeals.hubspot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DealStatusCountDto {
    private String status;
    private Long count;
}
