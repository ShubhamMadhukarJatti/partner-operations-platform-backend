package com.sharkdom.model.ppi;

import lombok.Data;

import java.util.List;

@Data
public class TriggerListResponse {
    private List<TriggerInfo> triggers;
}
