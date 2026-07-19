package com.sharkdom.quickstart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RewardsOverviewResponse {
    private List<RewardItemResponse> rewards;
    private long sendProposalCreditCount;
    private long dweepAICreditCount;
}