package com.sharkdom.repository.referral;

import com.sharkdom.entity.referral.InviteCampaignEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InviteCampaignRepository extends JpaRepository<InviteCampaignEntity, Long> {

    Optional<InviteCampaignEntity> findByPartnerIdAndCampaignId(Long partnerId, Long campaignId);
}