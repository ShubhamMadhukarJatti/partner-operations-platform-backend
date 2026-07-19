package com.sharkdom.entity.organization;

import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "integration_details")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class IntegrationDetails extends BaseEntity {
    private Long organizationId;
    @Column(length = 2048)
    private String refreshToken;
    public IntegrationType integrationType;
    private boolean isConnected;
    private String userId;
    private String connectedId;
    private String publishableKey;
    private String zohoTenantToken;
    private String zohoApiDomain;
}
