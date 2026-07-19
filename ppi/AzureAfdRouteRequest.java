package com.sharkdom.model.ppi;

import lombok.*;

import java.util.List;

@Data
public class AzureAfdRouteRequest {

    private Properties properties;

    @Data
    public static class Properties {
        private ResourceRef originGroup;
        private List<String> supportedProtocols;
        private List<String> patternsToMatch;
        private String forwardingProtocol;
        private String httpsRedirect;
        private List<ResourceRef> customDomains;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResourceRef {
        private String id;
    }
}
