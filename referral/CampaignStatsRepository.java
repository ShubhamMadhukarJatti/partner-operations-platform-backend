package com.sharkdom.repository.referral;

import com.sharkdom.entity.referral.CampaignStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignStatsRepository extends JpaRepository<CampaignStatsEntity, Long> {
    CampaignStatsEntity findByOrganizationId(Long organizationId);
}