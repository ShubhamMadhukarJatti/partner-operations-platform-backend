package com.sharkdom.model.ppi;

import lombok.Data;

@Data
public class Properties {
    private String hostName;
    private TlsSettings tlsSettings;
    private ValidationProperties validationProperties;
    private String domainValidationState;
    private String provisioningState;
    private String deploymentStatus;
}
