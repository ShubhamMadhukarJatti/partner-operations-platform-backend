package com.sharkdom.model.ppi;

import lombok.Data;

@Data
public class DeploymentRequest {
    private String manifestFileName;
    private String description;
    private int versionNumber;

}
