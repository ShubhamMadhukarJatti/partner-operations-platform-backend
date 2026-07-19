package com.sharkdom.model.campaign;

import com.sharkdom.constants.campaign.TriggerStatus;
import com.sharkdom.entity.campaign.Condition;

import java.util.List;

public record TriggerUpdateRequest(Long triggerId, TriggerStatus status, String campaignName, List<String> nodes,
                                   List<String> edges, List<Condition> conditions) {
}
