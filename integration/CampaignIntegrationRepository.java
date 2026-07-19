package com.sharkdom.repository.integration;

import com.sharkdom.constants.campaign.CampaignType;
import com.sharkdom.entity.campaign.CampaignIntegrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignIntegrationRepository extends JpaRepository<CampaignIntegrationEntity, Long> {
    List<CampaignIntegrationEntity> findByOrganizationId(Long organizationId);

    Optional<CampaignIntegrationEntity> findByOrganizationIdAndCampaignType(Long organizationId, CampaignType campaignType);

    Optional<CampaignIntegrationEntity> findByCampaignType(CampaignType campaignType);
}
