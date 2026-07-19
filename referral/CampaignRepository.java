package com.sharkdom.repository.referral;

import com.sharkdom.entity.referral.CampaignEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<CampaignEntity, Long> {

    List<CampaignEntity> findByOrganizationId(Long organizationId);

    @Query("SELECT c FROM CampaignEntity c WHERE c.partnerId = :organizationId")
    List<CampaignEntity> findJoinedOrganizationId(Long organizationId);

    CampaignEntity findByReferralCode(String referralCode);

    Optional<CampaignEntity> findTopByOrganizationIdAndPartnerId(Long organizationId, Long partnerId);
}