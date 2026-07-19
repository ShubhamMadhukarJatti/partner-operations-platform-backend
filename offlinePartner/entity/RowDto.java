package com.sharkdom.offlinePartner.entity;

import lombok.Data;

import java.util.Map;

@Data
public class RowDto {
    private Long rowId;
    private Map<String, String> cells;
}