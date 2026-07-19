package com.sharkdom.tablecustomization.dto.externalpartner;


import com.sharkdom.offlinePartner.entity.ColumnType;

public record ColumnSeed(
        String name,
        ColumnType type,
        Integer order
) {}

