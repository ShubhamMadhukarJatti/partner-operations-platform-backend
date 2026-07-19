package com.sharkdom.model.ppi;

import lombok.Data;

@Data
public class AzureCustomDomainRequest {

    private Properties properties;

    @Data
    public static class Properties {
        private String hostName;
        private TlsSettings tlsSettings;
    }

    @Data
    public static class TlsSettings {
        private String certificateType;
        private String minimumTlsVersion;
    }
}
