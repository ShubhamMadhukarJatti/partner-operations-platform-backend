package com.sharkdom.integration.model;

import com.sharkdom.constants.organization.IntegrationType;
import jakarta.validation.constraints.NotBlank;

public record IntegrationSaveRequest(Long organizationId,
                                     @NotBlank(message = "Refresh token should not be empty")
                                     String refreshToken,
                                     IntegrationType integrationType,
                                     boolean isConnected,
                                     String userId,
                                     String connectedId,
                                     String publishableKey) {


    public IntegrationSaveRequest setOrganizationId(Long organizationId) {
        return new IntegrationSaveRequest(organizationId, refreshToken, integrationType, isConnected, userId, connectedId, publishableKey);
    }

    public IntegrationSaveRequest setUserId(String userId) {
        return new IntegrationSaveRequest(organizationId, refreshToken, integrationType, isConnected, userId, connectedId, publishableKey);
    }
}
