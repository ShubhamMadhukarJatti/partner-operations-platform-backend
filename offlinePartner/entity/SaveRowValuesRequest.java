package com.sharkdom.offlinePartner.entity;

import lombok.Data;

import java.util.Map;

@Data
public class SaveRowValuesRequest {
    private Map<Long, String> cells;
}
