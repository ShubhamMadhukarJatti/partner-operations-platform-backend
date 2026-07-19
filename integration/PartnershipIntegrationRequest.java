package com.sharkdom.model.integration;

import java.util.List;

public record PartnershipIntegrationRequest(Long organizationId,
                                            String category,
                                            List<Endpoints> endpoints,
                                            List<String> sectorsAllowed,
                                            String docUrl,
                                            IntegrationType integrationType,
                                            String endpointUrl) {
    public enum IntegrationType {
        SWAGGER,
        POSTMAN_LINK,
        POSTMAN_JSON
    }
}
