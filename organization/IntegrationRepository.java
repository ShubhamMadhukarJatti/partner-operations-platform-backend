package com.sharkdom.repository.organization;

import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.entity.organization.IntegrationDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationRepository extends JpaRepository<IntegrationDetails, Long> {

    List<IntegrationDetails> findAllByOrganizationId(Long organizationId);

    IntegrationDetails findByOrganizationIdAndIntegrationType(Long organizationId, IntegrationType integrationType);

    boolean existsByOrganizationIdAndIntegrationType(Long organizationId, IntegrationType integrationType);

    boolean existsByUserIdAndIntegrationType(String userId, IntegrationType integrationType);

    boolean existsByOrganizationIdAndIntegrationTypeAndIsConnectedTrue(Long organizationId, IntegrationType integrationType);

    IntegrationDetails findByUserIdAndIntegrationType(String userId, IntegrationType integrationType);

    void deleteByOrganizationIdAndIntegrationType(Long organizationId, IntegrationType integrationType);

    void deleteByUserIdAndIntegrationType(String userId, IntegrationType integrationType);

    List<IntegrationDetails> findByIntegrationTypeAndIsConnectedAndRefreshTokenIsNotNull(IntegrationType type, boolean isConnected);

    Optional<IntegrationDetails> findByOrganizationId(Long organizationId);

    List<IntegrationDetails> findAllByOrganizationIdIn(List<Long> organizationIds);

    List<IntegrationDetails> findAllByOrganizationIdAndRefreshTokenIsNotNull(Long organizationId);

    IntegrationType IntegrationType(IntegrationType integrationType);

    List<IntegrationDetails> findAllByUserId(String userId);

    Optional<IntegrationDetails> findByOrganizationIdAndIntegrationTypeAndIsConnectedAndRefreshTokenIsNotNull(Long organizationId, IntegrationType integrationType, boolean isConnected);

    List<IntegrationDetails>
    findAllByIntegrationTypeAndIsConnectedTrueAndRefreshTokenIsNotNull(
            IntegrationType integrationType
    );

    List<IntegrationDetails> findByOrganizationIdAndIntegrationTypeIn(Long orgId, List<IntegrationType> types);

    @Query("""
       SELECT i
       FROM IntegrationDetails i
       WHERE i.organizationId = :organizationId
       ORDER BY
           CASE WHEN i.refreshToken IS NULL THEN 1 ELSE 0 END,
           i.lastUpdatedTimestamp DESC
       """)
    List<IntegrationDetails> findAllByOrganizationSorted(@Param("organizationId") Long organizationId);

    Optional<IntegrationDetails>
    findByZohoTenantToken(
            String tenantToken
    );
}


