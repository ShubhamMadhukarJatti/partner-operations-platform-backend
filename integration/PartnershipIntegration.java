package com.sharkdom.entity.integration;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.model.integration.PartnershipIntegrationRequest;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "partnership_integration")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnershipIntegration extends BaseEntity {
    private Long organizationId;
    private String category;
    @OneToMany(cascade = CascadeType.ALL)
    private List<Endpoints> endpoints;
    private List<String> sectorsAllowed;
    @Column(nullable = false)
    private String docUrl;
    private String endpointUrl;
    private PartnershipIntegrationRequest.IntegrationType integrationType;
}
