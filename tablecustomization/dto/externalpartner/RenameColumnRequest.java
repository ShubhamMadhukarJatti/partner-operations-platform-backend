package com.sharkdom.tablecustomization.dto.externalpartner;

import lombok.Data;

@Data
public class RenameColumnRequest {
    private Long columnId;
    private String newName;
}

