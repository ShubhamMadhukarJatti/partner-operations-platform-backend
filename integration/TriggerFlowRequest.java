package com.sharkdom.model.integration;

import com.sharkdom.entity.campaign.Condition;
import lombok.Data;

import java.util.List;

@Data
public class TriggerFlowRequest {
    private List<String> nodes;
    private List<String> edges;
    private List<Condition> conditions;
}
