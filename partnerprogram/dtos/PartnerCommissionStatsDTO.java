package com.sharkdom.partnerprogram.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerCommissionStatsDTO {

    private Double totalEarned;
    private Double pendingCommission;
    private String nextPayoutDate;
}