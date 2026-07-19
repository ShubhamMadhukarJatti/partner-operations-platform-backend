package com.sharkdom.quickstart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RewardItemResponse {
    private boolean isClaimed;
    private boolean isRewardClaimable;
    private String videoUrl;
    private String rewardType;
    private String thumbnailUrl;
}