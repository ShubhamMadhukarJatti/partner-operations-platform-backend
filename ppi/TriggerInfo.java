package com.sharkdom.model.ppi;

import lombok.Data;

@Data
public class TriggerInfo {
    private String functionName;
    private String triggerId;
    private String eventType;
    private String triggerSource;
}
