package com.sharkdom.quickstart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RewardProgressResponse {
    private Map<String, Boolean> data;
    private String videoUrl;
    private RewardType rewardType;
}
