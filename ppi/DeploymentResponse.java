package com.sharkdom.model.ppi;

import lombok.Data;

@Data
public class DeploymentResponse {
    private String deploymentId;
    private DeploymentConfig deploymentConfig;
    private String updateTime;
}
