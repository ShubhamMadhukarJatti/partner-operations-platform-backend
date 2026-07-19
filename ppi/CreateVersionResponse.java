package com.sharkdom.model.ppi;

import lombok.Data;

@Data
public class CreateVersionResponse {
    private String scriptId;
    private int versionNumber;
    private String description;
    private String createTime;
}
