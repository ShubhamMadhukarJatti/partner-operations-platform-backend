package com.sharkdom.model.ppi;

import lombok.Data;

@Data
public class DeploymentWrapper {

    private String scriptId;
    private String accessToken;
    private DeploymentRequest deploymentRequest;
}
