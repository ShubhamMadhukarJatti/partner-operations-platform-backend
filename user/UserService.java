package com.sharkdom.service.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.constants.user.UserRole;
import com.sharkdom.dto.*;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.stripe.StripeSubscriptionData;
import com.sharkdom.entity.user.*;
import com.sharkdom.entity.user.InviteTeamMemberStatus;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.model.user.UserEmail;
import com.sharkdom.model.user.UserSearchResponse;
import com.sharkdom.model.user.UserSearchResponseBase;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.stripe.StripeSubscriptionRepository;
import com.sharkdom.repository.user.InviteTeamMemberRepository;
import com.sharkdom.repository.user.SlackIntegrationRepository;
import com.sharkdom.repository.user.UserLastActivityTimeRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserLastActivityTimeRepository userLastActivityTimeRepository;
    @Value("${env}")
    private String env;

    @Autowired
    StripeSubscriptionRepository stripeSubscriptionRepository;

    @Value("${invite.link.expiry-hours}")
    private int inviteExpiryHours;

    @Autowired
    CollabRequestService collabRequestService;
    private static final String ALGORITHM = "AES";
    private final EmailService emailService;
    private final SlackIntegrationRepository slackIntegrationRepository;
    private final IntegrationRepository integrationRepository;
    private final SlackService slackService;
    private final OrganizationRepository organizationRepository;
    private final InviteTeamMemberRepository inviteTeamMemberRepository;

    public UserService(EmailService emailService, SlackIntegrationRepository slackIntegrationRepository, IntegrationRepository integrationRepository, SlackService slackService, OrganizationUserMappingRepository organizationUserMappingRepository, OrganizationRepository organizationRepository, InviteTeamMemberRepository inviteTeamMemberRepository) {
        this.emailService = emailService;
        this.slackIntegrationRepository = slackIntegrationRepository;
        this.integrationRepository = integrationRepository;
        this.slackService = slackService;
        this.organizationRepository = organizationRepository;
        this.inviteTeamMemberRepository = inviteTeamMemberRepository;
    }

    public User findById(Long id) throws Exception {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH37, String.valueOf(id)));
    }

    public User findByEmail(String email) throws Exception {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH38, String.valueOf(email)));
    }

    public ResponseEntity<User> findByUserId(String userId) {
        var user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH39, userId));
        if ("disabled".equalsIgnoreCase(user.getStatus())) {
            throw new ResourceNotFoundException(ErrorMessages.SH40, user.getEmail());
        } else {
            return ResponseEntity.ok(user);
        }
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH41, username));
    }

    @Transactional
    public User update(User updated) throws Exception {
        findById(updated.getId());
        return userRepository.save(updated);
    }

    @Transactional
    public User save(User updated) {
        return userRepository.save(updated);
    }

    @Transactional
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


    @Transactional
    public User patch(String userId, JsonPatch patch) throws Exception {
        User user = findByUserId(userId).getBody();
        User userPatched = applyPatchToUser(patch, user);
        return update(userPatched);
    }

    private User applyPatchToUser(JsonPatch patch, User targetUser) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(targetUser, JsonNode.class));
        return objectMapper.treeToValue(patched, User.class);
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsUserByUsername(username);
    }

    public HttpStatus updateUserLastActivityTime(String userId, Date lastActivityTime) {

        lastActivityTime = lastActivityTime == null ? new Date() : lastActivityTime;
        userLastActivityTimeRepository.save(new UserLastActivityTime(userId, lastActivityTime));
        return HttpStatus.OK;
    }

    public Page<UserSearchResponse> searchUser(String username, String tags, String city, String state, int page, int size) {
        //sort tags and replace comma by % to match like condition in native query
        tags = Util.sortCommaSeparatedString(tags, ",", "%");
        return addMoreFieldsInUserSearchResponse(userRepository.searchUser(username, tags, city, state, PageRequest.of(page, size)), page, size);
    }

    private Page<UserSearchResponse> addMoreFieldsInUserSearchResponse(Page<UserSearchResponseBase> responseBasePage, int page, int size) {
        List<UserSearchResponseBase> resposneBaseList = responseBasePage.getContent();
        Map<String, Integer> countsMap = collabRequestService.getCollabsCountForUsers(resposneBaseList
                .stream().map(e -> e.getUserId()).collect(Collectors.toList()));

        List<UserSearchResponse> searchResponseList = resposneBaseList
                .stream().map(e -> new UserSearchResponse(e, countsMap.getOrDefault(e.getUserId(), 0))).collect(Collectors.toList());
        return new PageImpl<>(searchResponseList, PageRequest.of(page, size), searchResponseList.size());
    }

    public Page<UserEmail> getEmailListByUserType(String userType, int page, int size) {

        return userRepository.getEmailListByUserType(userType, PageRequest.of(page, size));
    }

    public List<User> findAllByUserIdIn(List<String> userIdList) {
        return userRepository.findAllByUserIdIn(userIdList);
    }

    public List<User> findAllActive() {
        return userRepository.findAllByStatus("ACTIVE");
    }

    @Cacheable("cpTypeCounts")
    public Map<String, Integer> getPreferredCPTypeCounts() {
        Map<String, Integer> cpCountsMap = new HashMap<>();
        userRepository.getAllCPTypes().stream().forEach(cpTypes -> {
            String[] cpTypesArr = cpTypes.split(",");
            Arrays.stream(cpTypesArr).sequential().forEach(cpType -> {
                if (cpCountsMap.containsKey(cpType)) {
                    cpCountsMap.put(cpType, cpCountsMap.get(cpType) + 1);
                } else {
                    cpCountsMap.put(cpType, 1);
                }
            });
        });
        return cpCountsMap;
    }

    @Transactional
    public Map<String, String> addUSer(String email, Long organizationId, OrgUserRole role) {

        Optional<User> user = userRepository.findByEmail(email);
        String userId = user.isEmpty() ? RandomStringUtils.random(10, true, true) : user.get().getUserId();
        create(User.builder().email(email).userId(userId).build());
        if (!role.equals(OrgUserRole.ADMIN)) {
            List<StripeSubscriptionData> stripeSubscriptionDatas = stripeSubscriptionRepository.findByOrganizationId(organizationId);
            stripeSubscriptionDatas.forEach(item -> {
                if (item.getSeatLeft() > 0) {
                    item.setSeatLeft(item.getSeatLeft() - 1);
                }
            });
        }
        String encode = userId + ":" + organizationId + ":" + role + ":" + email;
        var encodedValue = encrypt(encode);
        String url;
        if (env.equalsIgnoreCase("dev")) {
            url = "https://dev.sharkdom.com/login?utm_register=" + encodedValue;
        } else {
            url = "https://sharkdom.com/login?utm_register=" + encodedValue;
        }
        emailService.sendByEmail("Email_verify", email, url, "");
        return Map.of("signupUrl", url);
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

    @Transactional
    public SlackIntegration saveSlackIntegration(String userId, String channelId) {
        var slackIntegration = SlackIntegration.builder()
                .userId(userId)
                .channelId(channelId).build();
        var saved = slackIntegrationRepository.save(slackIntegration);
        slackService.sendAvailability(userId, channelId);
        return slackIntegration;
    }

    @Transactional
    public Map<String, String> addUserV1(String email, List<OrgUserRole> roles) {
        Long organizationId = Util.getOrgIdFromToken();
        String name = organizationRepository.findNameById(organizationId);
        Optional<User> user = userRepository.findByEmail(email);
        Optional<InviteTeamMember> teamMember = inviteTeamMemberRepository.findByEmail(email);
        if (teamMember.isPresent()) {
            return Map.of("msg", "Invite Already Sent.");
        }
        String userId = user.isEmpty()
                ? generateUserId()
                : user.get().getUserId();

        create(User.builder().email(email).userId(userId).isTeamMemberUser(true).build());

        // Reduce seat count if user does not include ADMIN role
        boolean isAdmin = roles.contains(OrgUserRole.ADMIN);
        if (!isAdmin) {
            List<StripeSubscriptionData> subscriptionList = stripeSubscriptionRepository.findByOrganizationId(organizationId);
            subscriptionList.forEach(item -> {
                if (item.getSeatLeft() > 0) {
                    item.setSeatLeft(item.getSeatLeft() - 1);
                }
            });
        }

        // Convert list of roles to comma-separated string
        String rolesAsString = roles.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        String encode = userId + ":" + organizationId + ":" + rolesAsString + ":" + email;
        var encodedValue = encrypt(encode);

        String baseUrl = env.equalsIgnoreCase("dev")
                ? "https://dev.sharkdom.com/login?utm_register="
                : "https://sharkdom.com/login?utm_register=";

        String signupUrl = baseUrl + encodedValue;

        InviteTeamMember inviteTeamMember = new InviteTeamMember();
        inviteTeamMember.setUserId(userId);
        inviteTeamMember.setEmail(email);
        inviteTeamMember.setOrgId(organizationId);
        inviteTeamMember.setInviteTeamMemberStatus(InviteTeamMemberStatus.PENDING);
        inviteTeamMember.setRoles(roles);
        InviteTeamMember save = inviteTeamMemberRepository.save(inviteTeamMember);
        if (save != null) {
            log.info("Invite team member save success");
        }
        emailService.sendByEmailAddTeamMember("Email_verify", email, signupUrl, name);

        return Map.of("signupUrl", signupUrl);
    }

    public String generateUserId() {
        return UUID.randomUUID().toString();
    }


    public IntegrationDetails saveSlackToken(IntegrationDetails integrationDetails) {
        if (integrationRepository.existsByUserIdAndIntegrationType(integrationDetails.getUserId(), IntegrationType.SLACK)) {
            var integration = integrationRepository.findByUserIdAndIntegrationType(integrationDetails.getUserId(), IntegrationType.SLACK);
            integration.setRefreshToken(integrationDetails.getRefreshToken());
            return integrationRepository.save(integration);
        }
        return integrationRepository.save(integrationDetails);
    }

    public IntegrationDetails updateIntegration(IntegrationDetails integrationDetails) {
        var integration = integrationRepository.findByUserIdAndIntegrationType(integrationDetails.getUserId(), integrationDetails.getIntegrationType());
        integration.setRefreshToken(integrationDetails.getRefreshToken());
        return integrationRepository.save(integration);
    }

    public IntegrationDetails getSlackToken(String userId) {
        var details = integrationRepository.findByUserIdAndIntegrationType(userId, IntegrationType.SLACK);
        if (details == null) {
            throw new SharkdomException(ErrorMessages.SH127, userId, IntegrationType.SLACK.name());
        }
        return details;
    }

    @Transactional
    public DealUserFlagResponse enableContinueFreeDeal(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        user.setContinueFreeDeal(true);
        User save = userRepository.save(user);
        return DealUserFlagResponse.builder()
                .isContinueFreeDeal(save.isContinueFreeDeal())
                .userId(save.getUserId())
                .build();
    }

    @Transactional
    public PmUserFlagResponse enableContinueFreePartnerMapping(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setContinueFreePartnerMapping(true);
        User save = userRepository.save(user);
        return PmUserFlagResponse.builder()
                .isContinueFreePartnerMapping(save.isContinueFreePartnerMapping())
                .userId(save.getUserId())
                .build();
    }

    public UserFlagResponse getFreeDealFlag(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with userId: " + userId));

        return new UserFlagResponse(
                user.getUserId(),
                user.isContinueFreeDeal(),
                false // other flag not included in this API
        );
    }

    public UserFlagResponse getFreePartnerMappingFlag(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with userId: " + userId));

        return new UserFlagResponse(
                user.getUserId(),
                false, // other flag not included in this API
                user.isContinueFreePartnerMapping()
        );
    }

    public void consumeSeat(Long orgId) {
        log.info("[SEAT] Attempting to consume seat for orgId={}", orgId);


        List<StripeSubscriptionData> subs = stripeSubscriptionRepository.findByOrganizationId(orgId);


        for (StripeSubscriptionData sub : subs) {
            if (sub.getSeatLeft() > 0) {
                sub.setSeatLeft(sub.getSeatLeft() - 1);
                stripeSubscriptionRepository.save(sub);


                log.info("[SEAT] Seat consumed successfully | OrgId={} | SeatsLeft={}",
                        orgId, sub.getSeatLeft());
                return;
            }
        }


        log.error("[SEAT] No seats available for orgId={}", orgId);
        throw new ServiceException(ErrorMessages.SH174);
    }


    public void releaseSeat(Long orgId) {
        log.info("[SEAT] Releasing seat for orgId={}", orgId);


        List<StripeSubscriptionData> subs = stripeSubscriptionRepository.findByOrganizationId(orgId);


        for (StripeSubscriptionData sub : subs) {
            sub.setSeatLeft(sub.getSeatLeft() + 1);
            stripeSubscriptionRepository.save(sub);


            log.info("[SEAT] Seat released | OrgId={} | SeatsLeft={}",
                    orgId, sub.getSeatLeft());
            return;
        }
    }

    @Transactional
    public Map<String, String> addUserV3(String name,String email, List<OrgUserRole> roles) {
        var optionalOrganization = organizationRepository.findByPrimaryEmail(email);
        if (optionalOrganization.isPresent()) {
            return Map.of("msg", "An organization with this email already exists.");
        }

        log.info("[INVITE-V3] Starting invite process for email={}", email);


        Long orgId = Util.getOrgIdFromToken();
        String orgName = organizationRepository.findNameById(orgId);


        inviteTeamMemberRepository.findByEmail(email).ifPresent(i -> {
            log.warn("[INVITE-V3] Invite already exists for email={}", email);
            throw new ServiceException(ErrorMessages.SH107);
        });


        String userId = userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElseGet(this::generateUserId);


        log.info("[INVITE-V3] Using userId={}", userId);


        // Create user if not exists
        create(User.builder()
                .name(name)
                .email(email)
                .userId(userId)
                .isTeamMemberUser(true)
                .build());


        // Default role
        if (roles == null || roles.isEmpty()) {
            roles = List.of(OrgUserRole.USER);
            log.info("[INVITE-V3] Default USER role assigned to {}", email);
        }

        // Seat consumed on invite
        consumeSeat(orgId);


        LocalDateTime expiryTime = LocalDateTime.now().plusHours(inviteExpiryHours);


        String encoded = encrypt(userId + ":" + orgId + ":" + roles + ":" + email);


        String baseUrl = env.equalsIgnoreCase("dev")
                ? "https://dev.sharkdom.com/login?utm_register="
                : "https://sharkdom.com/login?utm_register=";


        String signupUrl = baseUrl + encoded;


        InviteTeamMember invite = new InviteTeamMember();
        invite.setUserId(userId);
        invite.setEmail(email);
        invite.setName(name);
        invite.setOrgId(orgId);
        invite.setInviteTeamMemberStatus(InviteTeamMemberStatus.PENDING);
        invite.setRoles(roles);
        invite.setExpiresAt(expiryTime);
        invite.setSeatConsumed(true);


        inviteTeamMemberRepository.save(invite);


        log.info("[INVITE-V3] Invite saved | email={} | expiresAt={}", email, expiryTime);


        emailService.sendByEmailAddTeamMember("Email_verify", email, signupUrl, orgName);


        log.info("[INVITE-V3] Invite email sent successfully to {}", email);


        return Map.of("msg", "Invite sent successfully!");
    }

    @Transactional
    public void activateInvite(String userId) {
        log.info("[INVITE-ACTIVATE] Activating invite for userId={}", userId);
        InviteTeamMember invite = inviteTeamMemberRepository.findByUserId(userId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH88, userId));
        if (invite.getInviteTeamMemberStatus() == InviteTeamMemberStatus.EXPIRED) {
            log.warn("[INVITE-ACTIVATE] Invite expired for userId={}", userId);
            throw new ServiceException(ErrorMessages.SH85);
        }
        invite.setInviteTeamMemberStatus(InviteTeamMemberStatus.ACTIVE);
        inviteTeamMemberRepository.save(invite);
        // Assign USER role in user table
        Role role = new Role();
        role.setRole(UserRole.USER);
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH39, userId));
        user.setRoles(List.of(role));
        user.setTeamMemberUser(true);
        userRepository.save(user);
        log.info("[INVITE-ACTIVATE] Invite activated & role assigned | userId={}", userId);
    }

    @Scheduled(fixedRate = 60000) // every 1 minute
    public void expireInvites() {


        log.info("[INVITE-EXPIRY] Running expiry scan");


        List<InviteTeamMember> expiredInvites =
                inviteTeamMemberRepository.findByInviteTeamMemberStatusAndExpiresAtBefore(
                        InviteTeamMemberStatus.PENDING,
                        LocalDateTime.now());


        for (InviteTeamMember invite : expiredInvites) {


            log.warn("[INVITE-EXPIRY] Expiring invite | email={} | orgId={}",
                    invite.getEmail(), invite.getOrgId());


            invite.setInviteTeamMemberStatus(InviteTeamMemberStatus.EXPIRED);


            if (invite.isSeatConsumed()) {
                releaseSeat(invite.getOrgId());
                invite.setSeatConsumed(false);
            }


            inviteTeamMemberRepository.save(invite);
        }


        log.info("[INVITE-EXPIRY] Expiry scan complete | expiredCount={}", expiredInvites.size());
    }

    public SharkdomApiResponse<List<InviteStatusResponseDTO>> getPendingAndExpiredInvites() {
        Long orgId = Util.getOrgIdFromToken();
        log.info("[INVITE-LIST] Fetching PENDING & EXPIRED invites | orgId={}", orgId);
        List<InviteTeamMemberStatus> statuses = List.of(
                InviteTeamMemberStatus.PENDING,
                InviteTeamMemberStatus.EXPIRED
        );
        List<InviteTeamMember> invites =
                inviteTeamMemberRepository.findByOrgIdAndInviteTeamMemberStatusIn(orgId, statuses);
        log.info("[INVITE-LIST] Found {} invites", invites.size());
        if (invites.isEmpty()) {
            return new SharkdomApiResponse<>(true, "No pending or expired invites found", List.of());
        }
        List<InviteStatusResponseDTO> responseList = new ArrayList<>();
        for (InviteTeamMember invite : invites) {
            InviteStatusResponseDTO dto = new InviteStatusResponseDTO();
            dto.setUserId(invite.getUserId());
            dto.setEmail(invite.getEmail());
            dto.setRoles(invite.getRoles());
            dto.setStatus(invite.getInviteTeamMemberStatus());
            dto.setExpiresAt(invite.getExpiresAt());
            dto.setSeatConsumed(invite.isSeatConsumed());
            // Convert Date → LocalDateTime from BaseEntity
            if (invite.getCreationTimestamp() != null) {
                dto.setRequestSentAt(
                        invite.getCreationTimestamp()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                );
            }
            dto.setName(invite.getName());
            responseList.add(dto);
            log.debug("[INVITE-LIST] Added invite | email={} | status={}",
                    dto.getEmail(), dto.getStatus());
        }
        log.info("[INVITE-LIST] Response prepared successfully");
        return new SharkdomApiResponse<>(true, "Invite list fetched successfully", responseList);
        }
}
