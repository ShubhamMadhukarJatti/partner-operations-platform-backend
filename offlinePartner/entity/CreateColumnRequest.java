package com.sharkdom.offlinePartner.entity;


import lombok.Data;

@Data
public class CreateColumnRequest {
    private String name;
    private ColumnType type;
    private Integer displayOrder;
    private Boolean visible;
}