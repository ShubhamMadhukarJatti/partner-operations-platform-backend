package com.sharkdom.service.partnerportalsnapshot;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.partnerportalsnapshot.PartnerPortalSnapShot;
import com.sharkdom.entity.partnerportalsnapshot.PartnerPortalSnapshotResponse;
import com.sharkdom.entity.user.User;
import com.sharkdom.enums.partnerportalsnapshot.PartnerPortalSnapshotAccess;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.partnerportalsnapshot.PartnerPortalSnapshotRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.util.Util;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class PartnerPortalSnapShotService {

    @Autowired
    private PartnerPortalSnapshotRepository partnerPortalSnapshotRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Value("${env}")
    private String env;

    private static final String ALGORITHM = "AES";

    @Autowired
    private EmailService emailService;


    public PartnerPortalSnapShot upsertSnapshot(
            Long senderOrgId,
            String receiverUserEmail,
            String receiverUserId,
            PartnerPortalSnapshotAccess accessLevel,
            boolean isProgressChartShared,
            boolean isNotesAndAttachmentsShared
    ) {

        log.info("Upserting snapshot for senderOrgId={}, receiverUserId={}", senderOrgId, receiverUserId);

        // Check existing snapshot (unique: senderOrgId + receiverUserId)
        PartnerPortalSnapShot existing = partnerPortalSnapshotRepository
                .findBySenderOrganizationIdAndReceiverUserId(senderOrgId, receiverUserId)
                .orElse(null);

        if (existing == null) {
            // Create new snapshot
            PartnerPortalSnapShot snapshot = new PartnerPortalSnapShot();
            snapshot.setSenderOrganizationId(senderOrgId);
            snapshot.setReceiverUserEmail(receiverUserEmail);
            snapshot.setReceiverUserId(receiverUserId);
            snapshot.setNotesAndAttachmentsShared(isNotesAndAttachmentsShared);
            snapshot.setProgressChartShared(isProgressChartShared);
            snapshot.setPartnerPortalSnapshotAccess(accessLevel);
            PartnerPortalSnapShot saved = partnerPortalSnapshotRepository.save(snapshot);
            log.info("Created new snapshot with ID {}", saved.getId());
            return saved;
        }

        // Update existing snapshot
        existing.setReceiverUserEmail(receiverUserEmail);
        existing.setPartnerPortalSnapshotAccess(accessLevel);
        existing.setNotesAndAttachmentsShared(isNotesAndAttachmentsShared);
        existing.setProgressChartShared(isProgressChartShared);
        PartnerPortalSnapShot updated = partnerPortalSnapshotRepository.save(existing);
        log.info("Updated existing snapshot with ID {}", updated.getId());

        return updated;
    }

    @Transactional
    public Map<String, Object> sharePortalSnapshot(
            String email,
            PartnerPortalSnapshotAccess access,
            String externalPartnerCode
    ) {

        Long senderOrgId = Util.getOrgIdFromToken();
        String name = organizationRepository.findNameById(senderOrgId);
        log.info("Sender Organization resolved | orgId={} | organizationName={}", senderOrgId, name);

        List<Map<String, String>> resultList = new ArrayList<>();
        int success = 0;
        int failed = 0;

            log.info("Processing snapshot share for email={}", email);

            try {
                // Check if user exists
                Optional<User> userOpt = userRepository.findByEmail(email);
                boolean isNewUser = userOpt.isEmpty();

                String userId = isNewUser
                        ? generateUserId()
                        : userOpt.get().getUserId();

                log.debug("User resolved | email={} | userId={} | isNewUser={}", email, userId, isNewUser);

                // Create user if not exist
                if (isNewUser) {
                    create(User.builder().email(email).userId(userId).build());
                    log.info("New user created | email={} | userId={}", email, userId);
                }

                // UPSERT Snapshot
                PartnerPortalSnapShot snapshot = partnerPortalSnapshotRepository
                        .findBySenderOrganizationIdAndReceiverUserId(senderOrgId, userId)
                        .orElse(new PartnerPortalSnapShot());

                log.debug("Preparing snapshot entity | senderOrgId={} | receiverUserId={}",
                        senderOrgId, userId);

                snapshot.setSenderOrganizationId(senderOrgId);
                snapshot.setReceiverUserEmail(email);
                snapshot.setReceiverUserId(userId);
                snapshot.setExternalPartnerCode(externalPartnerCode);
                snapshot.setPartnerPortalSnapshotAccess(access);
                log.info("Snapshot saved | email={} | access={}", email, access.name());
                // Generate secure invite link
                String payload = userId + ":" + senderOrgId + ":" + access.name() + ":" + email + ":" + externalPartnerCode;
                log.debug("Generating token payload for email={}", email);
                String encoded = encrypt(payload);
                log.debug("Payload encrypted successfully for email={}", email);
                String baseUrl = env.equalsIgnoreCase("dev")
                        ? "http://dev.sharkdom.com/partner-portal/login?utm="
                        : "https://sharkdom.com/partner-portal/login?utm=";

                String finalUrl = baseUrl + encoded;
                log.info("Invite URL generated | email={}", email);
                log.info("final url {}", finalUrl);
                log.info("final url {}", name);
                log.info("final url {}", email);
                snapshot.setShared_url(finalUrl);
                var save = partnerPortalSnapshotRepository.save(snapshot);

                // Send email
                emailService.sendByEmailAddTeamMember("partner_snapshot_invite", email, finalUrl, name);

                log.info("Invite email sent | email={}", email);

                // Add each result
                resultList.add(
                        Map.of(
                                "email", email,
                                "userId", userId,
                                "access", access.name(),
                                "shareUrl", finalUrl
                        )
                );

                success++;


            } catch (Exception e) {

            }


        return Map.of(
                "msg", "Snapshot shared successfully",
                "sharedUsers", resultList,
                "successCount", success,
                "failureCount", failed
        );
    }


    public String generateUserId() {
        return UUID.randomUUID().toString();
    }

    @org.springframework.transaction.annotation.Transactional
    public User save(User updated) {
        return userRepository.save(updated);
    }

    @org.springframework.transaction.annotation.Transactional
    public User create(User user) {
        log.error("create user request");
        return userRepository.findByUserId(user.getUserId())
                .map(existingUser -> {
                    existingUser.setUsername(user.getUsername());
                    existingUser.setName(user.getName());
                    existingUser.setGender(user.getGender());
                    existingUser.setMobile(user.getMobile());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setBriefDescription(user.getBriefDescription());
                    existingUser.setAbout(user.getAbout());
                    existingUser.setEmailVerified(user.isEmailVerified());
                    existingUser.setUserType(user.getUserType());
                    existingUser.setRiskAppetite(user.getRiskAppetite());
                    existingUser.setMint(user.getMint());
                    existingUser.setStatus(user.getStatus());
                    existingUser.setDeviceId(user.getDeviceId());
                    existingUser.setTags(user.getTags());
                    existingUser.setCity(user.getCity());
                    existingUser.setState(user.getState());
                    existingUser.setCanCollaborate(user.isCanCollaborate());
                    existingUser.setWebsite(user.getWebsite());
                    existingUser.setTrialPeriodProcured(user.isTrialPeriodProcured());
                    existingUser.setSector(user.getSector());

                    existingUser.setRoles(user.getRoles());
                    existingUser.setWorkExperiences(user.getWorkExperiences());
                    existingUser.setInterestAreas(user.getInterestAreas());
                    existingUser.setAdditionalDetails(user.getAdditionalDetails());

                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> userRepository.save(user));
    }

    public String encrypt(String data) {
        try {
            String key;
            if (env.equalsIgnoreCase("dev")) {
                key = "Uz1FyvLoNnIKdGjMIRPDKccr";
            } else {
                key = "KzaFdvfoDOIFd9SMIQPDKcE1";
            }
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new ServiceException(ErrorMessages.SH116, e.getMessage());
        }
    }

    public List<Map<String, Object>> getSnapshotsWithOrgName(String receiverUserId) {

        List<PartnerPortalSnapShot> snapshots = partnerPortalSnapshotRepository.findByReceiverUserId(receiverUserId);

        return snapshots.stream().map(s -> {
            String orgName = organizationRepository.findById(s.getSenderOrganizationId())
                    .map(Organization::getName)
                    .orElse("Unknown Organization");

            Map<String, Object> data = new HashMap<>();
            data.put("snapshotId", s.getId());
            data.put("senderOrgId", s.getSenderOrganizationId());
            data.put("senderOrgName", orgName);
            data.put("receiverUserId", s.getReceiverUserId());
            data.put("receiverUserEmail", s.getReceiverUserEmail());
            data.put("accessLevel", s.getPartnerPortalSnapshotAccess().name());
            data.put("progressChartShared", s.isProgressChartShared());
            data.put("notesAndAttachmentsShared", s.isNotesAndAttachmentsShared());
            data.put("sharedUrl", s.getShared_url());

            return data;
        }).toList();
    }

}
