package com.sharkdom.model.ppi;

import lombok.Data;

@Data
public class DeploymentConfig {
    private String scriptId;
    private int versionNumber;
    private String manifestFileName;
    private String description;
}
