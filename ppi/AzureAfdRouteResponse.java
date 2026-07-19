package com.sharkdom.model.ppi;

import lombok.Data;

@Data
public class AzureAfdRouteResponse {
    private String id;
    private String name;
    private String type;
    private Properties properties;

    @Data
    public static class Properties {
        private String provisioningState;
        private String deploymentStatus;
    }
}

