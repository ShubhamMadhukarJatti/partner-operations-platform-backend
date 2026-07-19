package com.sharkdom.registration.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.OrgUserMappingStatus;
import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.constants.user.UserRole;
import com.sharkdom.dto.IpInfoResponse;
import com.sharkdom.entity.organization.OrganizationUserMapping;
import com.sharkdom.entity.user.InviteTeamMember;
import com.sharkdom.entity.user.InviteTeamMemberStatus;
import com.sharkdom.entity.user.OrganizationUserRoleMapping;
import com.sharkdom.entity.user.User;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.exception.UnauthorizedException;
import com.sharkdom.offlinePartner.repository.OfflinePartnerInviteRepository;
import com.sharkdom.registration.entity.RefreshTokenEntity;
import com.sharkdom.registration.model.*;
import com.sharkdom.registration.repository.RefreshTokenRepository;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.user.InviteTeamMemberRepository;
import com.sharkdom.repository.user.OrganizationUserRoleMappingRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.security.IpUtil;
import com.sharkdom.security.JwtUtil;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.otp.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RegistrationService {
    private final OtpService otpService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OrganizationUserMappingRepository organizationUserMappingRepository;
    private final JwtUtil jwtUtil;
    private final OrganizationUserRoleMappingRepository roleMappingRepository;
    private final InviteTeamMemberRepository inviteTeamMemberRepository;
    private final EmailService emailService;
    private final OfflinePartnerInviteRepository offlinePartnerInviteRepository;

    public RegistrationService(OtpService otpService, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, OrganizationUserMappingRepository organizationUserMappingRepository, JwtUtil jwtUtil, OrganizationUserRoleMappingRepository roleMappingRepository, InviteTeamMemberRepository inviteTeamMemberRepository, EmailService emailService, OfflinePartnerInviteRepository offlinePartnerInviteRepository) {
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.organizationUserMappingRepository = organizationUserMappingRepository;
        this.jwtUtil = jwtUtil;
        this.roleMappingRepository = roleMappingRepository;
        this.inviteTeamMemberRepository = inviteTeamMemberRepository;
        this.emailService = emailService;
        this.offlinePartnerInviteRepository = offlinePartnerInviteRepository;
    }

    public Map<String, String> loginUser(UserLoginRequest request) {
        var user = userRepository.findByEmailAndIsActiveTrue(request.email());
        if (user.isPresent()) {
            return otpService.generateOTP(request.email(), "OTP_login_verify");
        }
        throw new ServiceException(ErrorMessages.SH03, request.email());
    }

    public Map<String, String> partnerPortalLogin(UserLoginRequest request) {
        offlinePartnerInviteRepository.findByEmail(request.email())
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH88, request.email()));
        return otpService.generateOTP(request.email(), "OTP_login_verify");
    }

    @Transactional
    public Map<String, String> registerUser(String email) {
        // Check if user already exists (only verified users should block re-registration)
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ServiceException(ErrorMessages.SH07, email);
        }

        // Generate and send OTP
        return otpService.generateOTP(email, "email_verify_otp");
    }


//    @Transactional
//    public LoginResponse verifyUser(UserVerifyRequest request) {
//        // Step 1: Validate OTP
//        otpService.validateOTP(request.email(), request.otp());
//
//        // Step 2: Check if user already exists
//        return userRepository.findByEmail(request.email())
//                .map(user -> {
//                    // If user exists, just return login response
//                    OrgUserRole role = organizationUserMappingRepository.findAllByUserId(user.getUserId())
//                            .stream()
//                            .findFirst()
//                            .map(OrganizationUserMapping::getRole)
//                            .orElse(OrgUserRole.ADMIN);
//
//                    return generateLoginResponse(user.getEmail(), role, user.getUserId());
//                })
//                .orElseGet(() -> {
//                    // Step 3: If user does not exist, create a new one after OTP success
//                    User newUser = new User();
//                    newUser.setUserId(generateUserId());
//                    newUser.setEmail(request.email());
//                    userRepository.save(newUser);
//
//                    // Default role for new user
//                    OrgUserRole role = OrgUserRole.ADMIN;
//
//                    return generateLoginResponse(newUser.getEmail(), role, newUser.getUserId());
//                });
//    }

    @Transactional
    public LoginResponse verifyUser(UserVerifyRequest request) {
        // Step 1: Validate OTP
        otpService.validateOTPV1(request.email(), request.otp());

        // Step 2: Check if user already exists
        return userRepository.findByEmail(request.email())
                .map(user -> {
                    // If user exists, mark verified if not already
                    if (!user.isEmailVerified()) {
                        user.setEmailVerified(true);
                        userRepository.save(user);
                    }


                    // Fetch role if exists, otherwise default to ADMIN
                    OrgUserRole role = organizationUserMappingRepository.findAllByUserId(user.getUserId())
                            .stream()
                            .findFirst()
                            .map(OrganizationUserMapping::getRole)
                            .orElse(OrgUserRole.ADMIN);

                    return generateLoginResponse(user.getEmail(), role, user.getUserId(),user.isOnboarded());
                })
                .orElseGet(() -> {
                    // If user does not exist, create a new one (already verified)
                    User newUser = new User();
                    newUser.setUserId(generateUserId());
                    newUser.setEmail(request.email());
                    newUser.setEmailVerified(true);
                    User save = userRepository.save(newUser);

                    OrgUserRole role = OrgUserRole.ADMIN;
                    return generateLoginResponse(newUser.getEmail(), role, newUser.getUserId(),save.isOnboarded());
                });
    }




    public String generateUserId() {
        return UUID.randomUUID().toString();
    }

    private LoginResponse generateLoginResponse(String email, OrgUserRole role, String userId,boolean onboarded) {
        String accessToken = jwtUtil.generateAccessToken(email, role);
        String refreshToken = jwtUtil.generateRefreshToken(email, role);
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setUserId(userId);
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setExpiryDate(calculateExpiryDate());
        refreshTokenRepository.save(refreshTokenEntity);
        return new LoginResponse(accessToken, refreshToken, userId,onboarded);
    }

    private LocalDateTime calculateExpiryDate() {
        long expirationTime = jwtUtil.getRefreshTokenExpirationTime();
        return LocalDateTime.now().plusMinutes(expirationTime);
    }

    @Transactional
    public LoginResponse createRefreshToken(RefreshTokenRequest request) {
        RefreshTokenEntity existingToken = findByRefreshToken(request.token());
        if (existingToken == null || isRefreshTokenExpired(existingToken)) {
            throw new UnauthorizedException(ErrorMessages.SH01);
        }

        String newToken = jwtUtil.generateRefreshToken(
                getUserEmailFromUserId(existingToken.getUserId()),
                getUserRoleFromUserId(existingToken.getUserId())
        );
        String accessToken = jwtUtil.generateAccessToken(
                getUserEmailFromUserId(existingToken.getUserId()),
                getUserRoleFromUserId(existingToken.getUserId())
        );

        // Create and save new refresh token entity
        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setUserId(existingToken.getUserId());
        refreshToken.setToken(newToken);
        refreshToken.setExpiryDate(calculateExpiryDate());

        refreshTokenRepository.save(refreshToken);
        deleteRefreshTokenByToken(request.token());
        return new LoginResponse(accessToken, newToken, existingToken.getUserId(),true);
    }

    private String getUserEmailFromUserId(String userId) {
        return userRepository.findByUserId(userId)
                .map(User::getEmail)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH110, userId));
    }

    private OrgUserRole getUserRoleFromUserId(String userId) {
        return organizationUserMappingRepository.findAllByUserId(userId)
                .stream()
                .findFirst()
                .map(OrganizationUserMapping::getRole)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH110, userId));
    }

    private void deleteRefreshTokenByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    private RefreshTokenEntity findByRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    private boolean isRefreshTokenExpired(RefreshTokenEntity token) {
        return token.getExpiryDate().isBefore(LocalDateTime.now());
    }

    @Transactional
    public List<OrganizationUserRoleMapping> createOrUpdateRoleMappings(Long orgUserMappingId, List<OrgUserRole> roles) {
        List<OrganizationUserRoleMapping> savedMappings = new ArrayList<>();
        String userId = organizationUserMappingRepository.findById(orgUserMappingId).get().getUserId();
        for (OrgUserRole role : roles) {
            OrganizationUserRoleMapping existing = roleMappingRepository
                    .findByOrgUserMappingIdAndRole(orgUserMappingId, role);

            if (existing != null) {
                // Update if needed (you can modify other fields here)
                savedMappings.add(roleMappingRepository.save(existing));
            } else {
                OrganizationUserRoleMapping newMapping = new OrganizationUserRoleMapping();
                newMapping.setOrgUserMappingId(orgUserMappingId);
                newMapping.setRole(role);
                newMapping.setUserId(userId);
                savedMappings.add(roleMappingRepository.save(newMapping));
                Optional<InviteTeamMember> optionalTeamMember = inviteTeamMemberRepository.findByUserId(userId);
                if (optionalTeamMember.isPresent()) {
                    InviteTeamMember teamMember = optionalTeamMember.get();
                    teamMember.setInviteTeamMemberStatus(InviteTeamMemberStatus.ACTIVE);
                    InviteTeamMember save = inviteTeamMemberRepository.save(teamMember);
                    if (save!=null)
                    {
                        log.info("done");
                    }
                }
            }
        }

        return savedMappings;
    }
    @Transactional
    public List<OrganizationUserRoleMapping> createOrUpdateRoleMappingsByUserId(
            String userId,
            List<OrgUserRole> newRoles) {

        log.info("Updating organization user roles for userId: {}", userId);

        // Step 1: Fetch existing mappings
        List<OrganizationUserRoleMapping> existingMappings =
                roleMappingRepository.findByUserId(userId);

        Set<OrgUserRole> existingRoles = existingMappings.stream()
                .map(OrganizationUserRoleMapping::getRole)
                .collect(Collectors.toSet());

        Set<OrgUserRole> newRoleSet = new HashSet<>(newRoles);

        // Step 2: REMOVE roles that are no longer needed
        for (OrganizationUserRoleMapping mapping : existingMappings) {
            if (!newRoleSet.contains(mapping.getRole())) {
                roleMappingRepository.delete(mapping);
                log.debug("Removed role {} for userId {}", mapping.getRole(), userId);
            }
        }

        // Step 3: ADD new roles
        OrganizationUserMapping orgMapping =
                organizationUserMappingRepository.findByUserId(userId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "OrganizationUserMapping not found for userId: " + userId));

        List<OrganizationUserRoleMapping> result = new ArrayList<>();

        for (OrgUserRole newRole : newRoleSet) {
            if (!existingRoles.contains(newRole)) {
                OrganizationUserRoleMapping newMapping = new OrganizationUserRoleMapping();
                newMapping.setUserId(userId);
                newMapping.setOrgUserMappingId(orgMapping.getId());
                newMapping.setRole(newRole);

                result.add(roleMappingRepository.save(newMapping));
                log.debug("Added new role {} for userId {}", newRole, userId);
            }
        }

        // Step 4: Return fresh data from DB
        return roleMappingRepository.findByUserId(userId);
    }


    @Transactional
    public LoginResponse externalPartnerVerifyUser(UserVerifyRequest request) {
        // Step 1: Validate OTP
        otpService.validateOTPV1(request.email(), request.otp());

        // Step 2: Check if user already exists
        return userRepository.findByEmail(request.email())
                .map(user -> {
                    // If user exists, mark verified if not already
                    if (!user.isEmailVerified()) {
                        user.setEmailVerified(true);
                        userRepository.save(user);
                    }

                    var optUser = organizationUserMappingRepository.findByUserId(user.getUserId());
                    if (optUser.isEmpty()) {
                        OrganizationUserMapping organizationUserMapping = new OrganizationUserMapping();
                        organizationUserMapping.setUserId(user.getUserId());
                        organizationUserMapping.setRole(OrgUserRole.PARTNER_PORTAL_USER);
                        organizationUserMapping.setStatus(OrgUserMappingStatus.ACTIVE);
                        organizationUserMappingRepository.save(organizationUserMapping);
                    }

                    return generateLoginResponse(user.getEmail(), OrgUserRole.PARTNER_PORTAL_USER, user.getUserId(),user.isOnboarded());
                })
                .orElseGet(() -> {
                    // If user does not exist, create a new one (already verified)
                    User newUser = new User();
                    newUser.setUserId(generateUserId());
                    newUser.setEmail(request.email());
                    newUser.setEmailVerified(true);
                    User save = userRepository.save(newUser);

                    OrgUserRole role = OrgUserRole.PARTNER_PORTAL_USER;
                    return generateLoginResponse(newUser.getEmail(), role, newUser.getUserId(),save.isOnboarded());
                });
    }

    public List<RefreshTokenResponse> getAllRefreshTokens() {

        List<RefreshTokenEntity> tokens = refreshTokenRepository.findAll();

        List<String> userIds = tokens.stream()
                .map(RefreshTokenEntity::getUserId)
                .distinct()
                .toList();

        var users = userRepository.findByUserIdIn(userIds);

        var userMap = users.stream()
                .collect(Collectors.toMap(
                        user -> user.getUserId(),
                        user -> user.getEmail()
                ));

        return tokens.stream()
                .map(token -> RefreshTokenResponse.builder()
                        .id(token.getId())
                        .token(token.getToken())
                        .expiryDate(token.getExpiryDate())
                        .userId(token.getUserId())
                        .userEmail(userMap.getOrDefault(token.getUserId(), "User Not Found"))
                        .generatedAt(token.getLastUpdatedTimestamp())
                        .build())
                .toList();
    }


    private User createPartnerUser(String email) {
        User newUser = new User();

        newUser.setUserId(generateUserId());
        newUser.setEmail(email);
        newUser.setEmailVerified(true);
        newUser.setActive(true);
        newUser.setUserType(UserRole.PARTNER.name());

        return userRepository.save(newUser);
    }

    @Transactional
    public LoginResponse verifyPartner(UserVerifyRequest request) {

        otpService.validatePartnerOTP(request.email(), request.otp());

        User user = userRepository.findByEmailAndIsActiveTrue(request.email())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUserId(generateUserId());
                    newUser.setEmail(request.email());
                    newUser.setEmailVerified(true);
                    newUser.setActive(true);
                    newUser.setUserType(UserRole.PARTNER.name());

                    return userRepository.save(newUser);
                });

        return generateLoginResponse(
                user.getEmail(),
                OrgUserRole.PARTNER_PORTAL_USER,
                user.getUserId(),
                user.isOnboarded()
        );
    }

    @Transactional
    public LoginResponse partnerPasswordLogin(com.sharkdom.partnerprogram.dtos.PartnerPasswordLoginRequest request) {

        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH03, request.getEmail()));

        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        if (user.getPassword() == null || !encoder.matches(request.getPassword(), user.getPassword())) {
            throw new com.sharkdom.exception.UnauthorizedException(ErrorMessages.SH202);
        }

        return generateLoginResponse(
                user.getEmail(),
                OrgUserRole.PARTNER_PORTAL_USER,
                user.getUserId(),
                user.isOnboarded()
        );
    }

    @Transactional
    public LoginResponse directLogin(String email) {

        // Step 1: Find active user
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH03, email));

        // Step 2: Fetch user role
        OrgUserRole role = organizationUserMappingRepository
                .findAllByUserId(user.getUserId())
                .stream()
                .findFirst()
                .map(OrganizationUserMapping::getRole)
                .orElse(OrgUserRole.ADMIN);

        // Step 3: Directly generate login response
        return generateLoginResponse(
                user.getEmail(),
                role,
                user.getUserId(),
                user.isOnboarded()
        );
    }
}