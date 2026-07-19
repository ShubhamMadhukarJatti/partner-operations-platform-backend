package com.sharkdom.model.email;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TriggerTypeModel {
    private TriggerType triggerType;
    private String templateCode;
    private List<String> exclusions;
    private LocalDate updatedBefore;
    private LocalDate updatedAfter;
    private String scheduledAt;
}
