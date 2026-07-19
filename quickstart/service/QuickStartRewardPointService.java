package com.sharkdom.quickstart.service;

import com.sharkdom.entity.credits.Credit;
import com.sharkdom.model.PersonaStatus;
import com.sharkdom.quickstart.dto.RewardItemResponse;
import com.sharkdom.quickstart.dto.RewardType;
import com.sharkdom.quickstart.dto.RewardsOverviewResponse;
import com.sharkdom.quickstart.entity.QuickStartRewardAssignment;
import com.sharkdom.quickstart.entity.QuickStartRewardPointVideos;
import com.sharkdom.quickstart.repository.QuickStartRewardAssignmentRepository;
import com.sharkdom.quickstart.repository.QuickStartRewardPointVideosRepository;
import com.sharkdom.repository.ai.PersonaStatusRepository;
import com.sharkdom.repository.credits.CreditRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationCollaborationRepository;
import com.sharkdom.repository.partnerDeals.DealRepository;
import com.sharkdom.util.Util;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class QuickStartRewardPointService {

    @Autowired
    private OrganizationCollaborationRepository organizationCollaborationRepository;

    @Autowired
    private QuickStartRewardPointVideosRepository quickStartRewardPointVideosRepository;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private CreditRepository creditRepository;

    @Autowired
    private OrganizationUserMappingRepository organizationUserMappingRepository;

    @Autowired
    private PersonaStatusRepository personaStatusRepository;

    @Autowired
    private QuickStartRewardPointVideosRepository rewardConfigRepo;

    @Autowired
    private QuickStartRewardAssignmentRepository rewardAssignmentRepository;

    public RewardsOverviewResponse getRewardsOverview() {
        Long orgId = Util.getOrgIdFromToken();
        log.info("Fetching rewards overview for organizationId: {}", orgId);

        RewardsOverviewResponse response = new RewardsOverviewResponse();
        List<RewardItemResponse> rewards = new ArrayList<>();

        try {
            Map<RewardType, QuickStartRewardPointVideos> videoMap = fetchRewardVideos();
            rewards = evaluateRewardMilestones(orgId, videoMap);
            response.setRewards(rewards);
            response.setDweepAICreditCount(fetchCreditCount(orgId));

            log.info("Rewards overview generated successfully for organizationId: {}", orgId);

        } catch (Exception e) {
            log.error("Error while generating rewards overview for orgId {}: {}", orgId, e.getMessage(), e);
        }

        return response;
    }

    /**
     * Fetch all QuickStartRewardPointVideos by RewardType
     */
    private Map<RewardType, QuickStartRewardPointVideos> fetchRewardVideos() {
        log.debug("Fetching QuickStartRewardPointVideos for all reward types...");

        Map<RewardType, QuickStartRewardPointVideos> map = new EnumMap<>(RewardType.class);
        for (RewardType type : RewardType.values()) {
            quickStartRewardPointVideosRepository.findByRewardType(type)
                    .ifPresent(video -> map.put(type, video));
        }

        log.debug("Fetched reward videos for {} reward types", map.size());
        return map;
    }

    /**
     * Evaluate all milestones and create reward items accordingly
     */
    private List<RewardItemResponse> evaluateRewardMilestones(Long orgId, Map<RewardType, QuickStartRewardPointVideos> videoMap) {
        List<RewardItemResponse> rewards = new ArrayList<>();

        // 1. Company Profile Completion (always claimable)
        rewards.add(createRewardItem(orgId, RewardType.COMPANY_PROFILE_COMPLETION, true, videoMap));

        // 2. First Partner Added
        long collaborationCount = organizationCollaborationRepository.countAllCollaborations(orgId);
        boolean isPartnerMilestoneReached = collaborationCount > 0;
        log.info("FIRST_PARTNER_ADDED milestone reached: {} for orgId: {}", isPartnerMilestoneReached, orgId);
        rewards.add(createRewardItem(orgId, RewardType.FIRST_PARTNER_ADDED, isPartnerMilestoneReached, videoMap));

        // 3. First Deal Registered
        long dealsCount = dealRepository.countByDealerOrgId(orgId);
        boolean isDealMilestoneReached = dealsCount > 0;
        log.info("FIRST_DEAL_REGISTERED milestone reached: {} for orgId: {}", isDealMilestoneReached, orgId);
        rewards.add(createRewardItem(orgId, RewardType.FIRST_DEAL_REGISTERED, isDealMilestoneReached, videoMap));

        // 4. Teammate Invited
        long teammateCount = organizationUserMappingRepository.countByOrganizationId(orgId);
        boolean isTeammateMilestoneReached = teammateCount > 1;
        log.info("TEAMMATE_INVITED milestone reached: {} for orgId: {}", isTeammateMilestoneReached, orgId);
        rewards.add(createRewardItem(orgId, RewardType.TEAMMATE_INVITED, isTeammateMilestoneReached, videoMap));

        // 5. Data Source Connected
        boolean crmConnected = personaStatusRepository
                .findFirstByOrganizationIdAndPersonaStatus(orgId, PersonaStatus.COMPLETED)
                .isPresent();
        log.info("DATA_SOURCE_CONNECTED milestone reached: {} for orgId: {}", crmConnected, orgId);
        rewards.add(createRewardItem(orgId, RewardType.DATA_SOURCE_CONNECTED, crmConnected, videoMap));

        // 6. Partner Opportunity Found (future milestone)
        rewards.add(createRewardItem(orgId, RewardType.PARTNER_OPPORTUNITY_FOUND, false, videoMap));

        // 7. Ecosystem Goal Achieved (future milestone)
        rewards.add(createRewardItem(orgId, RewardType.ECOSYSTEM_GOAL_ACHIEVED, false, videoMap));

        log.info("All reward milestones evaluated successfully for orgId: {}", orgId);
        return rewards;
    }

    /**
     * Build single RewardItemResponse with proper logging
     */
    /**
     * Build single RewardItemResponse with proper logging
     */
    private RewardItemResponse createRewardItem(Long orgId, RewardType type, boolean isClaimable,
                                                Map<RewardType, QuickStartRewardPointVideos> videoMap) {
        boolean isClaimed = rewardAssignmentRepository.existsByOrganizationIdAndRewardType(orgId, type);

        String videoUrl = null;
        String thumbnailUrl = null;

        if (videoMap.containsKey(type)) {
            QuickStartRewardPointVideos video = videoMap.get(type);
            videoUrl = video.getVideoUrl();
            thumbnailUrl = video.getThumbnailUrl();
        }

        log.debug("Creating RewardItemResponse → type: {}, claimable: {}, claimed: {}, videoUrl: {}, thumbnailUrl: {}",
                type, isClaimable, isClaimed, videoUrl, thumbnailUrl);

        return RewardItemResponse.builder()
                .isRewardClaimable(isClaimable)
                .isClaimed(isClaimed)
                .rewardType(type.name())
                .videoUrl(videoUrl)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    /**
     * Fetch Dweep AI credit count for organization
     */
    private int fetchCreditCount(Long orgId) {
        log.debug("Fetching Dweep AI credit count for orgId: {}", orgId);
        Optional<Integer> credits = creditRepository.findCreditsByOrgId(orgId);
        int count = credits.orElse(0);
        log.info("Dweep AI credit count for orgId {}: {}", orgId, count);
        return count;
    }

    @Transactional
    public Map<String, Object> saveQuickStartDetails(RewardType rewardType) {
        Long orgIdFromToken = Util.getOrgIdFromToken();
        log.info("Received request to add QuickStart reward points for organizationId: {} and rewardType: {}", orgIdFromToken, rewardType);

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<QuickStartRewardPointVideos> optReward = quickStartRewardPointVideosRepository.findByRewardType(rewardType);

            if (optReward.isEmpty()) {
                log.warn("Reward configuration not found for rewardType: {}", rewardType);
                response.put("success", false);
                response.put("message", "No reward configuration found for reward type: " + rewardType);
                return response;
            }

            QuickStartRewardPointVideos reward = optReward.get();
            int halfPoints = Math.toIntExact(reward.getRewardPoint() / 2);

            // Check if this reward is already assigned
            boolean alreadyAssigned = rewardAssignmentRepository.existsByOrganizationIdAndRewardType(orgIdFromToken, rewardType);
            if (alreadyAssigned) {
                log.info("RewardType {} already assigned to organizationId: {}. Skipping credit addition.", rewardType, orgIdFromToken);
                response.put("success", false);
                response.put("message", "Reward already assigned for this reward type.");
                return response;
            }

            // Try to fetch existing credit record
            Optional<Credit> optCredit = creditRepository.findByOrgId(orgIdFromToken);

            Credit credit;
            int previousCredits;
            int updatedCredits;

            if (optCredit.isEmpty()) {
                // Create new credit record
                credit = new Credit();
                credit.setOrgId(orgIdFromToken);
                credit.setCredits(halfPoints);
                creditRepository.save(credit);

                previousCredits = 0;
                updatedCredits = halfPoints;

                log.info("Created new credit record for organizationId: {} with {} initial credits (rewardType: {})",
                        orgIdFromToken, halfPoints, rewardType);

                response.put("isNewRecordCreated", true);
            } else {
                // Update existing record
                credit = optCredit.get();
                previousCredits = credit.getCredits();
                updatedCredits = previousCredits + halfPoints;
                credit.setCredits(updatedCredits);
                creditRepository.save(credit);

                log.info("Updated existing credit record for organizationId: {}. Added {} points for rewardType: {}. Total credits: {}",
                        orgIdFromToken, halfPoints, rewardType, updatedCredits);

                response.put("isNewRecordCreated", false);
            }

            // Save reward assignment
            QuickStartRewardAssignment assignment = new QuickStartRewardAssignment();
            assignment.setOrganizationId(orgIdFromToken);
            assignment.setRewardType(rewardType);
            rewardAssignmentRepository.save(assignment);

            log.info("Saved QuickStartRewardAssignment for organizationId: {} and rewardType: {}", orgIdFromToken, rewardType);

            response.put("success", true);
            response.put("rewardType", rewardType.toString());
            response.put("halfPointsAdded", halfPoints);
            response.put("previousCredits", previousCredits);
            response.put("updatedCredits", updatedCredits);
            response.put("message", "Reward points successfully processed and assignment recorded.");

        } catch (Exception ex) {
            log.error("Error processing QuickStart reward points for organizationId: {} and rewardType: {}. Message: {}",
                    orgIdFromToken, rewardType, ex.getMessage(), ex);

            response.put("success", false);
            response.put("message", "Failed to process reward points for " + rewardType);
        }
        return response;
    }

}
