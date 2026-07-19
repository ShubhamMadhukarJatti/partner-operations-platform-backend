package com.sharkdom.partnerprogram.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerDashboardStatsDTO {

    private Integer totalLeadsSubmitted;

    private Integer leadsInProgress;

    private BigDecimal commissionEarned;

    private BigDecimal commissionPending;
}