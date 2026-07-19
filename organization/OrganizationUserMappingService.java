package com.sharkdom.service.organization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.sharkdom.config.AppProperties;
import com.sharkdom.config.WebSocketHandler;
import com.sharkdom.constants.Constants;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.OrgUserMappingStatus;
import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.dto.OrganizationUserMappingResponseDTO;
import com.sharkdom.entity.credits.Credit;
import com.sharkdom.entity.notification.Notification;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organization.OrganizationUserMapping;
import com.sharkdom.entity.user.InviteTeamMember;
import com.sharkdom.entity.user.InviteTeamMemberStatus;
import com.sharkdom.entity.user.OrganizationUserRoleMapping;
import com.sharkdom.entity.user.User;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.model.email.TemplateOrganizationEmailReqModel;
import com.sharkdom.model.organization.OrganizationUserMappingResponse;
import com.sharkdom.model.organization.OrganizationWithOrganizationMappingResponse;
import com.sharkdom.quickstart.entity.QuickStartRewardAssignment;
import com.sharkdom.quickstart.repository.QuickStartRewardAssignmentRepository;
import com.sharkdom.repository.credits.CreditRepository;
import com.sharkdom.repository.email.EmailVerificationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.subscription.SubscriptionRepository;
import com.sharkdom.repository.user.InviteTeamMemberRepository;
import com.sharkdom.repository.user.OrganizationUserRoleMappingRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.notification.NotificationService;
import com.sharkdom.service.user.LoginNotificationService;
import com.sharkdom.util.Util;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sharkdom.constants.Constants.SIGNUP_FOLLOWUP_TEMPLATE;

@Slf4j
@Service
public class OrganizationUserMappingService {
    @Value("${app.environment.proxy_url}")
    String baseUrl;
    private OrganizationUserMappingRepository organizationUserMappingRepository;
    private final ScheduledExecutorService scheduler;

    private OrganizationUserMappingRequestService organizationUserMappingRequestService;
    private final LoginNotificationService loginNotificationService;
    private final UserRepository userRepository;
    private ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final AppProperties appProperties;
    private final OrganizationRepository organizationRepository;
    private final EmailService emailService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final WebSocketHandler webSocketHandler;
    private final OrganizationUserRoleMappingRepository organizationUserRoleMappingRepository;
    private final InviteTeamMemberRepository inviteTeamMemberRepository;
    private final QuickStartRewardAssignmentRepository quickStartRewardAssignmentRepository;
    private final CreditRepository creditRepository;


    public OrganizationUserMappingService(OrganizationUserMappingRepository organizationUserMappingRepository, OrganizationUserMappingRequestService organizationUserMappingRequestService, LoginNotificationService loginNotificationService, UserRepository userRepository, ObjectMapper objectMapper, NotificationService notificationService, AppProperties appProperties, OrganizationRepository organizationRepository, EmailService emailService, EmailVerificationRepository emailVerificationRepository, SubscriptionRepository subscriptionRepository, WebSocketHandler webSocketHandler, OrganizationUserRoleMappingRepository organizationUserRoleMappingRepository, InviteTeamMemberRepository inviteTeamMemberRepository, QuickStartRewardAssignmentRepository quickStartRewardAssignmentRepository, CreditRepository creditRepository) {
        this.organizationUserMappingRepository = organizationUserMappingRepository;
        this.loginNotificationService = loginNotificationService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.organizationRepository = organizationRepository;
        this.webSocketHandler = webSocketHandler;
        this.organizationUserRoleMappingRepository = organizationUserRoleMappingRepository;
        this.inviteTeamMemberRepository = inviteTeamMemberRepository;
        this.quickStartRewardAssignmentRepository = quickStartRewardAssignmentRepository;
        this.creditRepository = creditRepository;
        this.scheduler = Executors.newScheduledThreadPool(10);
        this.organizationUserMappingRequestService = organizationUserMappingRequestService;
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
        this.emailService = emailService;
        this.emailVerificationRepository = emailVerificationRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public Optional<OrganizationUserMapping> findByOrganizationIdAndUserId(long id, String userId) {
        return organizationUserMappingRepository.findByOrganizationIdAndUserId(id, userId);
    }

    public OrganizationUserMapping findById(long id) {
        return organizationUserMappingRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(ErrorMessages.SH31, id));
    }

    @Transactional
    public OrganizationUserMapping update(OrganizationUserMapping updated) throws Exception {
        findById(updated.getId());
        return organizationUserMappingRepository.save(updated);
    }

    @Transactional
    public OrganizationUserMapping create(OrganizationUserMapping organizationUserMapping) {

        if (organizationUserMapping == null) {
            throw new ServiceException(ErrorMessages.SH05);
        }

        if (organizationUserMapping.getOrganizationId() == null) {
            throw new ServiceException(ErrorMessages.SH21);
        }

        if (organizationUserMapping.getUserId() == null || organizationUserMapping.getUserId().isBlank()) {
            throw new ServiceException(ErrorMessages.SH39, "null");
        }

        try {
            Optional<OrganizationUserMapping> optional =
                    organizationUserMappingRepository.findByOrganizationIdAndUserId(
                            organizationUserMapping.getOrganizationId(),
                            organizationUserMapping.getUserId()
                    );

            if (optional.isPresent()) {
                String designation = organizationUserMapping.getDesignation();
                organizationUserMapping = optional.get();
                organizationUserMapping.setStatus(OrgUserMappingStatus.ACTIVE);
                organizationUserMapping.setDesignation(designation);
            }

            if (organizationUserMapping.getStatus() == OrgUserMappingStatus.UNAPPROVED) {
                organizationUserMappingRequestService
                        .saveRequestMappingAndSendApprovalMail(organizationUserMapping);
            }

            OrganizationUserMapping response =
                    organizationUserMappingRepository.save(organizationUserMapping);

            String templateCode =
                    appProperties.getEmailTemplateCodeForEvent(SIGNUP_FOLLOWUP_TEMPLATE);

            String secondTemplateCode =
                    appProperties.getEmailTemplateCodeForEvent(Constants.SIGNUP_SECOND_FOLLOWUP_TEMPLATE);

            Notification notification = Notification.builder()
                    .subject("Welcome to Sharkdom")
                    .body("You have successfully joined Sharkdom. We are excited to have you on board.")
                    .forWeb(true)
                    .organizationId(response.getOrganizationId())
                    .build();

            webSocketHandler.sendMessageToUser(response.getOrganizationId(), notification);
            notificationService.create(notification);

            scheduleEmail(templateCode, response, 30, TimeUnit.MINUTES);
            scheduleEmail(secondTemplateCode, response, 390, TimeUnit.MINUTES);

            Optional<Credit> optionalCredit =
                    creditRepository.findByOrgId(response.getOrganizationId());

            if (optionalCredit.isEmpty()) {
                Credit credit = new Credit();
                credit.setOrgId(response.getOrganizationId());
                credit.setCredits(2);
                creditRepository.save(credit);
            }

            return response;

        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException(ErrorMessages.SH116, ex.getMessage());
        }
    }


    public void scheduleEmail(String templateCode, OrganizationUserMapping organizationUserMapping, long delay, TimeUnit timeUnit) {
        log.info("scheduling email for "+ templateCode + ", " + organizationUserMapping.getOrganizationId() + ", " + organizationUserMapping.getUserId()) ;
        scheduler.schedule(() -> sendEmail(templateCode, organizationUserMapping), delay, timeUnit);
    }

    private void sendEmail(String templateCode, OrganizationUserMapping organizationUserMapping) {
        emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                .templateCode(templateCode)
                .organizationCode(organizationRepository.findCodeById(organizationUserMapping.getOrganizationId()))
                .organizationIds(List.of(organizationUserMapping.getOrganizationId())).build(), null, 1L, 1L);
    }

    public List<Organization> findAllOrganizationsByUserId(String userId, OrgUserMappingStatus mappingStatus) {
        return organizationUserMappingRepository.findAllOrganizationsByUserId(userId, mappingStatus);
    }

    public List<OrganizationWithOrganizationMappingResponse>
    findAllOrganizationsWithOrganizationMappingsByUserId(HttpServletRequest request, String userId) {

        if (userId == null || userId.isBlank()) {
            throw new ServiceException(ErrorMessages.SH39, "null");
        }

        try {
            List<OrganizationWithOrganizationMappingResponse> response =
                    organizationUserMappingRepository
                            .findAllOrganizationsWithOrganizationMappingsByUserId(userId);

            response.forEach(mappingResponse -> {
                Long orgId = mappingResponse.getOrganization().getId();

                var subscription =
                        subscriptionRepository
                                .findFirstByOrganizationIdOrderByCreationTimestampDesc(orgId);

                String plan = "FREE";
                if (subscription.isPresent()) {
                    plan = subscription.get().getPlanCode();
                }

                mappingResponse.getOrganization().setPlanCode(plan);
            });

            userRepository.findByUserId(userId).ifPresentOrElse(user -> {
                String clientIp = getClientIp(request);
                loginNotificationService
                        .checkAndNotifyNewIpLogin(clientIp, user.getEmail());
            }, () -> {
                throw new ServiceException(ErrorMessages.SH39, userId);
            });

            return response;

        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException(ErrorMessages.SH116, ex.getMessage());
        }
    }



    public List<OrganizationUserMappingResponse> findAllByOrganizationId(long id) {
        return organizationUserMappingRepository.findAllByOrganizationId(id);
    }

    public List<OrganizationUserMappingResponse> findAllByOrganizationIdAndStatus(long id, OrgUserMappingStatus status) {
        return organizationUserMappingRepository.findAllByOrganizationIdAndStatus(id, status);
    }

    public OrganizationUserMapping changeStatus(long id, String userId, OrgUserMappingStatus newStatus) {
        if (findByOrganizationIdAndUserId(id, userId).isEmpty()) {
            log.error("OrganizationUserMapping not found for given organizationId " + id + " and userId " + userId);
            throw new ResourceNotFoundException(ErrorMessages.SH130, id, userId);
        }
        OrganizationUserMapping organizationUserMapping = findByOrganizationIdAndUserId(id, userId).get();
        organizationUserMapping.setStatus(newStatus);
        return organizationUserMappingRepository.save(organizationUserMapping);
    }

    @Transactional
    public OrganizationUserMapping patch(long organizationId, String userId, JsonPatch patch) throws Exception {
        Optional<OrganizationUserMapping> optionalOrganization = findByOrganizationIdAndUserId(organizationId, userId);
        optionalOrganization.orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH32, organizationId, userId));
        OrganizationUserMapping organizationUserMapping = optionalOrganization.get();
        OrganizationUserMapping organizationUserPatched = applyPatchOrganizationUserMapping(patch, organizationUserMapping);
        return organizationUserMappingRepository.save(organizationUserPatched);
    }

    private OrganizationUserMapping applyPatchOrganizationUserMapping(JsonPatch patch, OrganizationUserMapping targetOrganizationUserMapping) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(targetOrganizationUserMapping, JsonNode.class));
        return objectMapper.treeToValue(patched, OrganizationUserMapping.class);
    }

    public List<OrganizationUserMapping> findAllByUserId(String userId) {
        return organizationUserMappingRepository.findAllByUserId(userId);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String ip = null;

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            ip = xForwardedFor.split(",")[0].trim();
        } else {
            ip = request.getRemoteAddr();
        }

        // Remove port if present
        if (ip != null && ip.contains(":")) {
            ip = ip.split(":")[0];
        }

        return ip;
    }

    public List<OrganizationUserMappingResponseDTO> findAllByOrganizationId() {
        log.info("Entering findAllByOrganizationId()");

        Long orgIdFromToken = Util.getOrgIdFromToken();
        log.debug("Extracted organization ID from token: {}", orgIdFromToken);

        List<OrganizationUserMappingResponseDTO> responseList = new ArrayList<>();

        List<InviteTeamMember> inviteTeamMemberList = inviteTeamMemberRepository.findByOrgId(orgIdFromToken);
        log.debug("Found {} invited team members for organization ID: {}", inviteTeamMemberList.size(), orgIdFromToken);

        for (InviteTeamMember inviteTeamMember : inviteTeamMemberList) {
            String userId = inviteTeamMember.getUserId();
            log.debug("Processing invite for user ID: {}", userId);

            // --- Handle Roles Safely ---
            List<OrgUserRole> userRoles = Optional.ofNullable(inviteTeamMember.getRoles())
                    .orElseGet(ArrayList::new);

            if (userRoles.isEmpty()) {
                userRoles.add(OrgUserRole.ADMIN);
                log.debug("No roles found for user ID {} — added default ADMIN role", userId);
            }

            // --- Fetch User Details ---
            Optional<User> optionalUser = userRepository.findByUserId(userId);
            if (optionalUser.isEmpty()) {
                log.warn("No user found for user ID: {}", userId);
                continue;
            }

            User user = optionalUser.get();

            // --- Build Response DTO ---
            OrganizationUserMappingResponseDTO responseDTO = new OrganizationUserMappingResponseDTO();
            responseDTO.setName(resolveDisplayName(user));
            responseDTO.setEmail(user.getEmail());
            if (inviteTeamMember.getInviteTeamMemberStatus().equals(InviteTeamMemberStatus.ACTIVE)) {
                List<OrgUserRole> userRoleList=new ArrayList<>();
                List<OrganizationUserRoleMapping> userRoleMappings = organizationUserRoleMappingRepository.findByUserId(userId);
                for (OrganizationUserRoleMapping userRoleMapping : userRoleMappings) {
                    OrgUserRole role = userRoleMapping.getRole();
                    userRoleList.add(role);
                }
                responseDTO.setRoles(userRoleList);
            }
            else {
                responseDTO.setRoles(userRoles);
            }
            responseDTO.setRequestSendDate(user.getCreationTimestamp());
            responseDTO.setUserId(user.getUserId());
            responseDTO.setOrganizationId(inviteTeamMember.getOrgId());

            // --- Handle Invite Status ---
            InviteTeamMemberStatus inviteStatus = inviteTeamMember.getInviteTeamMemberStatus();
            if (inviteStatus == null) {
                inviteStatus = InviteTeamMemberStatus.PENDING;
                log.debug("Invite status not set for user ID {} — defaulting to PENDING", userId);
            }
            responseDTO.setInviteTeamMemberStatus(inviteStatus);

            // --- Add to List ---
            responseList.add(responseDTO);
            log.debug("Added response DTO for user: {} ({})", responseDTO.getName(), responseDTO.getEmail());
        }

        log.info("Completed findAllByOrganizationId(). Total responses: {}", responseList.size());
        return responseList;
    }

    /**
     * Helper method to safely resolve display name.
     * Prefers name → email → "N/A".
     */
    private String resolveDisplayName(User user) {
        if (user.getName() != null && !user.getName().trim().isEmpty()) {
            return user.getName().trim();
        } else {
            return "N/A";
        }
    }

    public List<OrganizationUserMappingResponseDTO> findAllByOrganization() {

        log.info("Entering findAllByOrganizationId() [ACTIVE USERS ONLY]");

        Long orgId = Util.getOrgIdFromToken();
        log.debug("Extracted organization ID from token: {}", orgId);

        // --- Fetch ONLY ACTIVE Invites ---
        List<InviteTeamMember> invites =
                inviteTeamMemberRepository.findAllByOrgIdAndInviteTeamMemberStatus(
                        orgId,
                        InviteTeamMemberStatus.ACTIVE
                );

        if (invites.isEmpty()) {
            log.info("No ACTIVE team members found for orgId={}", orgId);
            return Collections.emptyList();
        }

        // --- Collect User IDs ---
        List<String> userIds = invites.stream()
                .map(InviteTeamMember::getUserId)
                .filter(Objects::nonNull)
                .toList();

        // --- Fetch Users in One Query ---
        Map<String, User> userMap = userRepository.findByUserIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        // --- Fetch Roles in One Query ---
        Map<String, List<OrgUserRole>> roleMap =
                organizationUserRoleMappingRepository.findByUserIdIn(userIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                OrganizationUserRoleMapping::getUserId,
                                Collectors.mapping(
                                        OrganizationUserRoleMapping::getRole,
                                        Collectors.toList()
                                )
                        ));

        List<OrganizationUserMappingResponseDTO> responseList = new ArrayList<>();

        for (InviteTeamMember invite : invites) {

            String userId = invite.getUserId();
            User user = userMap.get(userId);

            if (user == null) {
                log.warn("No user found for userId={}", userId);
                continue;
            }

            List<OrgUserRole> roles =
                    roleMap.getOrDefault(userId, Collections.emptyList());

            OrganizationUserMappingResponseDTO dto = new OrganizationUserMappingResponseDTO();
            dto.setName(resolveDisplayName(user));
            dto.setEmail(user.getEmail());
            dto.setUserId(userId);
            dto.setOrganizationId(invite.getOrgId());
            dto.setInviteTeamMemberStatus(InviteTeamMemberStatus.ACTIVE);
            dto.setRoles(roles);
            dto.setRequestSendDate(invite.getCreationTimestamp()); // Invite sent time

            responseList.add(dto);

            log.debug("Added ACTIVE user: {} ({})", dto.getName(), dto.getEmail());
        }

        log.info("Completed findAllByOrganizationId(). Total ACTIVE users: {}", responseList.size());
        return responseList;
    }

}
