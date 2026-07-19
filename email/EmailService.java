package com.sharkdom.service.email;

import com.amazonaws.services.simpleemail.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.campaign.CampaignStatus;
import com.sharkdom.constants.stripe.StripePlanType;
import com.sharkdom.entity.demo.DemoBook;
import com.sharkdom.entity.email.*;
import com.sharkdom.entity.integration.BooleanEvaluation;
import com.sharkdom.entity.integration.EvaluationEntity;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organization.OrganizationUserMapping;
import com.sharkdom.entity.stripe.StripeCheckoutSessions;
import com.sharkdom.entity.stripe.StripePlanConfiguration;
import com.sharkdom.entity.stripe.StripeSubscriptionData;
import com.sharkdom.entity.user.User;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.model.email.*;
import com.sharkdom.model.organization.OrganizationUserMappingResponse;
import com.sharkdom.model.ses.DomainVerificationResponse;
import com.sharkdom.model.user.UserEmailId;
import com.sharkdom.partnerprogram.dtos.PartnerApplicationDTO;
import com.sharkdom.partnerprogram.entities.CompanyPartnerApplication;
import com.sharkdom.partnerprogram.entities.ConsultantPartnerApplication;
import com.sharkdom.partnerprogram.entities.PartnerApplication;
import com.sharkdom.repository.email.*;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.referral.CampaignRepository;
import com.sharkdom.repository.stripe.StripePlanConfigurationRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.requirement.entity.CommunityOptIn;
import com.sharkdom.requirement.entity.TalentNetwork;
import com.sharkdom.util.Util;
import com.sharkdom.util.aws.service.SESDomainIdentityService;
import com.stripe.model.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.sharkdom.util.GeneralUtils.*;

@Service
@Slf4j
public class EmailService {

    @Autowired
    AmazonSes amazonSes;
    @Value("${env}")
    private String env;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    @Lazy
    EmailFromTemplateService emailFromTemplateService;
    @Autowired
    private EmailForwardStatsRepository emailStatsRepository;
    @Value("${email.default-sender}")
    private String defaultSender;
    @Value("${app.environment.proxy_url}")
    private String baseUrl;
    private final EmailUpdateRepository emailUpdateRepository;

    private final SESDomainIdentityService sesDomainIdentityService;
    private final DomainIdentityRepository domainIdentityRepository;

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final OrganizationUserMappingRepository organizationUserMappingRepository;
    private final CampaignRepository campaignRepository;
    private final EmailSubscribedRepository emailSubscribedRepository;
    private final StripePlanConfigurationRepository stripePlanConfigurationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmailService(EmailUpdateRepository emailUpdateRepository, SESDomainIdentityService sesDomainIdentityService, DomainIdentityRepository domainIdentityRepository, OrganizationRepository organizationRepository, UserRepository userRepository, EmailVerificationRepository emailVerificationRepository, OrganizationUserMappingRepository organizationUserMappingRepository, CampaignRepository campaignRepository, EmailSubscribedRepository emailSubscribedRepository, StripePlanConfigurationRepository stripePlanConfigurationRepository) {
        this.emailUpdateRepository = emailUpdateRepository;
        this.sesDomainIdentityService = sesDomainIdentityService;
        this.domainIdentityRepository = domainIdentityRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.organizationUserMappingRepository = organizationUserMappingRepository;
        this.campaignRepository = campaignRepository;
        this.emailSubscribedRepository = emailSubscribedRepository;
        this.stripePlanConfigurationRepository = stripePlanConfigurationRepository;
    }

    public List<EmailReqModelWithResponse> sendMultiple(List<EmailReqModelWithMultipartAttachments> emails, String templateCode, Long collaborationId, Long mouId) {
        List<EmailReqModelWithResponse> emailReqModelWithResponses =
                emails.stream()
                        .map(mailEntity -> new EmailReqModelWithResponse(mailEntity, send(mailEntity, templateCode, collaborationId, mouId)))
                        .collect(Collectors.toList());
        return emailReqModelWithResponses;
    }

    public EmailReqModelWithResponse sendOne(EmailReqModelWithMultipartAttachments mailEntity) {
        // String firestoreDocName = saveToFirebase(List.of(new EmailReqModelWithResponse(mailEntity)));
        try {
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(mailEntity.getBodyHtml());
            if (null == template.getSender() || template.getSender().isEmpty()) {
                mailEntity.setFrom(defaultSender);
            } else {
                mailEntity.setFrom(template.getSender());
            }
            String templateCode = mailEntity.getBodyHtml();
            if (template.getBodyHtml().contains("<java>{{unsubscribe.link}}</java>")) {
                String unsubscribeLink = "https://sharkdom.com/unsubscribe?email=" + mailEntity.getRecipients().get(0);
                var x = template.getBodyHtml().replace("<java>{{unsubscribe.link}}</java>", unsubscribeLink);
                template.setBodyHtml(x);
            }
            mailEntity.setBodyHtml(template.getBodyHtml());
            mailEntity.setSubject(template.getSubject());
            mailEntity.setBodyText(template.getBodyString());
            return new EmailReqModelWithResponse(mailEntity, send(mailEntity, templateCode, 1L, 1L));
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException(ErrorMessages.SH116, e.getMessage());
        }
    }

    private ResponseEntity<String> send(EmailReqModelWithMultipartAttachments mailEntity, String templateCode, Long collaborationId, Long mouId) {
        try {
            amazonSes.prepareAndSend(mailEntity.getSubject(), mailEntity.getBodyHtml(), mailEntity.getS3AttachmentNames(), mailEntity.getFrom(), String.join(",", mailEntity.getRecipients()), mailEntity.getAttachmentList(), templateCode, collaborationId);
            return new ResponseEntity<>("Mail sent successfully to " + String.join(",", mailEntity.getRecipients()), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error from third party while sending email " + e.getMessage());
            return new ResponseEntity<>("Error from third party " + e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public List<EmailReqModelWithResponse> sendByTemplateAndUserIds(TemplateEmailReqModel templateEmailReqModel, Map<String, Map<String, Object>> additionalData) {
        log.error("inside email service for user: {}", Arrays.toString(templateEmailReqModel.getUserIds().toArray(new String[0])));
        try {
            EmailTemplate firebaseTemplate = emailFromTemplateService.getTemplateByCode(templateEmailReqModel.getTemplateCode());
            EmailTemplate template = replaceJavaTagsUser(firebaseTemplate, templateEmailReqModel);

            if (null == template.getSender() || template.getSender().isEmpty()) {
                template.setSender(defaultSender);
            }

            //if s3AttachmentNames is present in request then it will replace the s3AttachmentNames present in template configuration in firestore
            if (null != templateEmailReqModel.getS3AttachmentNames() && !templateEmailReqModel.getS3AttachmentNames().isEmpty()) {
                template.setS3AttachmentNames(templateEmailReqModel.getS3AttachmentNames());
            }
            List<EmailReqModelWithMultipartAttachments> emailReqModelWithMultipartAttachments = emailFromTemplateService.prepareEmailsForUsers(template, templateEmailReqModel.getUserIds(), additionalData);
            log.error("Sending email to{}", emailReqModelWithMultipartAttachments);
            return sendMultiple(emailReqModelWithMultipartAttachments, templateEmailReqModel.getTemplateCode(), 1L, 1L);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while preparing emails from template {}", e.getMessage());
            throw new ServiceException(ErrorMessages.SH144, e.getMessage());
        }
    }

    public List<EmailReqModelWithResponse> sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel templateOrganizationEmailReqModel, Map<String, Map<String, Object>> additionalData, Long collaborationId, Long mouId) {
        try {
            EmailTemplate firebaseTemplate = emailFromTemplateService.getTemplateByCode(templateOrganizationEmailReqModel.getTemplateCode());
            EmailTemplate template = replaceJavaTags(firebaseTemplate, templateOrganizationEmailReqModel);
            String senderEmail = template.getSender();
            String senderName = templateOrganizationEmailReqModel.getSenderOrganizationName();
            if (null == template.getSender() || template.getSender().isEmpty()) {
                senderEmail = defaultSender;
            }
            if (senderName != null && !senderName.isEmpty()) {
                template.setSender(String.format("%s via SharkDom <%s>", senderName, senderEmail));
            } else {
                template.setSender(String.format("Team SharkDom <%s>", senderEmail));
            }

            //if s3AttachmentNames is present in request then it will replace the s3AttachmentNames present in template configuration in firestore
            if (null != templateOrganizationEmailReqModel.getS3AttachmentNames() && !templateOrganizationEmailReqModel.getS3AttachmentNames().isEmpty()) {
                template.setS3AttachmentNames(templateOrganizationEmailReqModel.getS3AttachmentNames());
            }
            List<EmailReqModelWithMultipartAttachments> emailReqModelWithMultipartAttachments = emailFromTemplateService.prepareEmailsForOrganizations(template, templateOrganizationEmailReqModel.getOrganizationIds(), additionalData);
            return sendMultiple(emailReqModelWithMultipartAttachments, templateOrganizationEmailReqModel.getTemplateCode(), collaborationId, mouId);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while preparing emails from template {}", e.getMessage());
            throw new ServiceException(ErrorMessages.SH116, e.getMessage());
        }
    }

    public void scheduleTask(TriggerTypeModel model) {
        try{
            Instant scheduledInstant = OffsetDateTime.parse(model.getScheduledAt()).toInstant();
            log.info("Scheduling email trigger for {}", scheduledInstant);
            taskScheduler.schedule(() -> sendTriggeredEmails(model), Date.from(scheduledInstant));
        }
        catch (Exception exception){
            log.info("Error while scheduling tasks for scheduled task {}", exception.getMessage());
            sendTriggeredEmails(model);
        }

    }

    public void sendTriggeredEmails(TriggerTypeModel triggerTypeModel) {
        String triggerType = triggerTypeModel.getTriggerType().name();
        String templateCode = triggerTypeModel.getTemplateCode();
        List<String> exclusions = triggerTypeModel.getExclusions() != null ? triggerTypeModel.getExclusions() : List.of();

        switch (TriggerType.valueOf(triggerType.toUpperCase())) {
            case NOT_MIGRATED:
                sendUserNotMigratedEmails(templateCode, exclusions);
                break;
            case NOT_KYB:
                sendNotKybEmails(templateCode, exclusions);
                break;
            case PROFILE_NOT_COMPLETED:
                sendProfileNotCompleteMail(templateCode, exclusions);
                break;
            case USER_ONE_TIME:
                sendOneTimeUserEmail(templateCode, exclusions, triggerTypeModel.getUpdatedAfter(), triggerTypeModel.getUpdatedBefore());
                break;
            case ORG_ONE_TIME:
                sendOneTimeOrgEmail(templateCode, exclusions, triggerTypeModel.getUpdatedAfter(), triggerTypeModel.getUpdatedBefore());
                break;
            case EMAIL_SUBSCRIBED:
                sendSubscribedEmail(templateCode, exclusions, triggerTypeModel.getUpdatedAfter(), triggerTypeModel.getUpdatedBefore());
                break;
            case UNVERIFIED_EMAIL_ORG:
                sendUnverifiedEmail(templateCode, exclusions);
                break;
            case UPDATED_BEFORE:
                sendUpdatedBeforeEmails(templateCode, exclusions, triggerTypeModel.getUpdatedBefore());
                break;
        }

    }

    private void sendUpdatedBeforeEmails(String templateCode, List<String> exclusions, LocalDate updatedBefore) {
        organizationRepository.getAllOrganizationsUpdatedBefore(updatedBefore)
                .stream()
                .filter(orgId -> !exclusions.contains(orgId.toString()))
                .forEach(id -> sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                                .organizationName(organizationRepository.findNameById(id))
                                .templateCode(templateCode)
                                .organizationIds(List.of(id)).build(),
                        null, 1L, 1L));
    }

    private void sendUnverifiedEmail(String templateCode, List<String> exclusions) {
        organizationRepository.findAllUnverified()
                .stream()
                .filter(orgId -> !exclusions.contains(orgId.toString()))
                .forEach(organization -> {
                    String code = generateVerificationToken(16);
                    String transaction = generateVerificationToken(10);
                    String verificationLink = generateVerificationLink(baseUrl, code, transaction).build().toUriString();
                    EmailVerification emailVerification = EmailVerification.builder()
                            .organizationId(organization)
                            .userId("UserId")
                            .transactionId(transaction)
                            .verificationCode(code)
                            .expiresAt(calculateExpirationTime())
                            .build();
                    emailVerificationRepository.save(emailVerification);
                    sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                            .emailVerifyLink(verificationLink)
                            .templateCode(templateCode)
                            .organizationCode(organizationRepository.findCodeById(organization))
                            .organizationName(organizationRepository.findNameById(organization))
                            .organizationIds(List.of(organization)).build(), null, 1L, 1L);
                });
    }

    private void sendSubscribedEmail(String templateCode, List<String> exclusions, LocalDate updateAfter, LocalDate updateBefore) {
        List<EmailSubscribed> allEmailSubscribed;
        if (updateAfter != null && updateBefore != null) {
            Date start = toStartOfDay(updateAfter);
            Date end = toNextDayStart(updateBefore);
            allEmailSubscribed = emailSubscribedRepository.findAllEmailsBetweenDates(start, end);
        } else {
            allEmailSubscribed = emailSubscribedRepository.findAllEmails();
        }
        allEmailSubscribed.stream()
                .filter(email -> !exclusions.contains(email.getEmail()))
                .forEach(email -> sendByEmail(templateCode, email.getEmail(), null, null));
    }

    public static Date toStartOfDay(LocalDate date) {
        ZoneId zoneId = ZoneId.of("UTC"); // or "Asia/Kolkata" based on your business
        return Date.from(date.atStartOfDay(zoneId).toInstant());
    }

    public static Date toNextDayStart(LocalDate date) {
        ZoneId zoneId = ZoneId.of("UTC");
        return Date.from(date.plusDays(1).atStartOfDay(zoneId).toInstant());
    }

    private void sendOneTimeOrgEmail(String templateCode, List<String> exclusions, LocalDate updateAfter, LocalDate updateBefore) {
        List<Organization> orgIds;
        if (updateAfter != null && updateBefore != null) {
            Date start = toStartOfDay(updateAfter);
            Date end = toNextDayStart(updateBefore);
            log.info("Fetching organizations with active status and email unsubscribed between dates: {} and {}", start, end);
            orgIds = organizationRepository.getAllOrganizationsStatusActiveAndEmailUnsubscribedBetweenDates(start, end);
            log.info("Fetched organizations for one-time email between dates: {} and {}: {}", start, end, orgIds.size());
        } else {
            orgIds = organizationRepository.getAllOrganizationsStatusActiveAndEmailUnsubscribed();
        }
        orgIds.stream()
                .filter(org -> !exclusions.contains(org.getId().toString()))
                .forEach(org -> sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                                .organizationName(org.getName())
                                .templateCode(templateCode)
                                .organizationIds(List.of(org.getId())).build(),
                        null, 1L, 1L));
    }

    private void sendOneTimeUserEmail(String templateCode, List<String> exclusions, LocalDate updateAfter, LocalDate updateBefore) {
        List<UserEmailId> userIds;
        if (updateAfter != null && updateBefore != null) {
            Date start = toStartOfDay(updateAfter);
            Date end = toNextDayStart(updateBefore);
            userIds = userRepository.getAllUsersIdBetweenDates(start, end);
        } else {
            userIds = userRepository.getAllUsersId();
        }
        var res = userIds.stream()
                .filter(Objects::nonNull)
                .filter(userEmailId -> userEmailId.getEmail() != null)
                .filter(userEmailId -> !exclusions.contains(userEmailId.getEmail()))
                .filter(userEmailId -> userEmailId.getUserId() != null)
                .filter(userEmailId -> userEmailId.getName() != null)
                .toList();

        res.forEach(userEmailId -> {
            String unsubscribeLink = "";
            if (env.equalsIgnoreCase("dev")) {
                unsubscribeLink = "https://dev.sharkdom.com/unsubscribe?email=" + userEmailId.getEmail();
            } else {
                unsubscribeLink = "https://sharkdom.com/unsubscribe?email=" + userEmailId.getEmail();
            }
            sendByTemplateAndUserIds(TemplateEmailReqModel.builder()
                    .templateCode(templateCode)
                    .emailUnsubscribeLink(unsubscribeLink)
                    .userIds(List.of(userEmailId.getUserId()))
                    .username(userEmailId.getName()).build(), null);
        });
    }

    private void sendProfileNotCompleteMail(String templateCode, List<String> exclusions) {
        List<Long> orgIds = organizationRepository.findIdsByAboutNull()
                .stream()
                .filter(orgId -> !exclusions.contains(orgId.toString()))
                .toList();

        sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                        .templateCode(templateCode)
                        .organizationIds(orgIds).build(),
                null, 1L, 1L);
    }

    private void sendNotKybEmails(String templateCode, List<String> exclusions) {
        List<Long> orgIds = organizationRepository.findIdsByUnverified()
                .stream()
                .filter(orgId -> !exclusions.contains(orgId.toString()))
                .toList();

        sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                        .templateCode(templateCode)
                        .organizationIds(orgIds).build(),
                null, 1L, 1L);
    }

    private void sendUserNotMigratedEmails(String templateCode, List<String> exclusions) {
        List<String> userIds = userRepository.findUsersWithoutOrganizationMapping()
                .stream()
                .filter(userId -> !exclusions.contains(userId))
                .toList();

        sendByTemplateAndUserIds(TemplateEmailReqModel.builder()
                        .templateCode(templateCode)
                        .userIds(userIds).build(),
                null);
        System.out.println(userIds);
    }

    public EmailSubscribed subscribe(String email) {
        EmailSubscribed emailSubscribed = EmailSubscribed.builder().email(email).build();
        if (emailSubscribedRepository.existsByEmail(email)) {
            throw new SharkdomException(ErrorMessages.SH102, email);
        }
        return emailSubscribedRepository.save(emailSubscribed);
    }

    public List<EmailSubscribed> getAllSubscribedEmails() {
        return emailSubscribedRepository.findAll();
    }

    public Map<String, String> sendEmailUpdate(EmailUpdateRequest emailReqModel) throws ExecutionException, InterruptedException {

        String otp = RandomStringUtils.randomNumeric(6);

        emailUpdateRepository.save(EmailUpdateEntity.builder()
                .newEmail(emailReqModel.getNewEmail())
                .originalEmail(emailReqModel.getOriginalEmail())
                .otp(otp)
                .build());

        EmailTemplate template = emailFromTemplateService.getTemplateByCode("email_otp");
        if (template == null) {
            throw new ResourceNotFoundException(ErrorMessages.SH145);
        }

        String processedBodyHtml = template.getBodyHtml()
                .replace("{{verification.code}}", otp);

        try {
            amazonSes.prepareAndSend(
                    template.getSubject(),
                    processedBodyHtml,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    emailReqModel.getNewEmail(),
                    List.of(),
                    "email_update",
                    0L
            );
            return Map.of("message", "OTP sent successfully to " + emailReqModel.getNewEmail());
        } catch (Exception e) {
            log.error("Failed to send Email of otp {}", e.getMessage());
            throw new ServiceException(ErrorMessages.SH116, e.getMessage());
        }
    }

    public Map<String, String> verifyUpdateEmail(String originalEmail, String otp) {
        var update = emailUpdateRepository.findByOriginalEmailAndOtp(originalEmail, otp);
        if (update.isEmpty()) {
            throw new RuntimeException("Invalid OTP or email");
        }
        var emailUpdate = update.get();
        var organization = organizationRepository.findByPrimaryEmail(originalEmail);
        if (organization.isEmpty()) {
            throw new RuntimeException("Organization not found with email: " + originalEmail);
        }
        var org = organization.get();

        org.setPrimaryEmail(emailUpdate.getNewEmail());
        organizationRepository.save(org);

        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(originalEmail);
            String uid = userRecord.getUid();
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid);
            request.setEmail(emailUpdate.getNewEmail()).setEmailVerified(true);
            FirebaseAuth.getInstance().updateUser(request);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Failed to update email in firebase: " + e.getMessage(), e);
        }

        emailUpdateRepository.delete(emailUpdate);

        return Map.of("message", "Email updated Successfully to " + emailUpdate.getNewEmail());
    }


    @Transactional
    public ResponseEntity<EmailVerify> verifyEmail(String transaction, String code, String referralCode) {
        var response = emailVerificationRepository.findByTransactionIdAndVerificationCode(transaction, code);
        if (response.isEmpty()) {
            return new ResponseEntity<>(EmailVerify.builder().status(EmailVerifyStatus.INVALID_CODE).build(), HttpStatus.BAD_REQUEST);
        } else {
            var emailVerify = response.get();
            if (emailVerify.isUsed()) {
                return new ResponseEntity<>(new EmailVerify(EmailVerifyStatus.CODE_ALREADY_USED), HttpStatus.BAD_REQUEST);
            } else if (emailVerify.getExpiresAt().before(Date.from(Instant.now()))) {
                return new ResponseEntity<>(new EmailVerify(EmailVerifyStatus.CODE_EXPIRED), HttpStatus.BAD_REQUEST);
            } else {
                if (referralCode != null) {
                    var campaign = campaignRepository.findByReferralCode(referralCode);
                    if (campaign != null) {
                        campaign.setEmailVerified(true);
                        URL url;
                        try {
                            url = new URL(campaign.getUrlRef());
                            String siteDomain = url.getHost();
                            String[] emailParts = campaign.getEmailRef().split("@");
                            String emailDomain = emailParts[1];
                            if (siteDomain.equalsIgnoreCase(emailDomain)) {
                                campaign.setStatus(CampaignStatus.ACTIVE);
                            }
                            campaign.setDomainVerified(siteDomain.equalsIgnoreCase(emailDomain));
                            campaign.setDomain(siteDomain);
                            campaignRepository.save(campaign);
                            emailVerify.setUsed(true);
                            emailVerificationRepository.save(emailVerify);
                            return new ResponseEntity<>(new EmailVerify(EmailVerifyStatus.VERIFICATION_SUCCESSFUL), HttpStatus.OK);
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                var userId = emailVerify.getUserId();
                var orgId = emailVerify.getOrganizationId();
                var user = userRepository.findByUserId(userId);
                user.ifPresent(userDetails -> {
                    userDetails.setEmailVerified(true);
                    userRepository.save(userDetails);
                });
                var org = organizationRepository.findById(orgId);
                org.ifPresent(orgDetails -> {
                    orgDetails.setPrimaryEmailVerified("true");
                    organizationRepository.save(orgDetails);
                });
                emailVerify.setUsed(true);
                emailVerificationRepository.save(emailVerify);
                return new ResponseEntity<>(new EmailVerify(EmailVerifyStatus.VERIFICATION_SUCCESSFUL), HttpStatus.OK);
            }
        }
    }

    @Transactional
    public void resendVerificationEmail(Long organizationId) {
        var response = organizationUserMappingRepository.findAllByOrganizationId(organizationId);
        response.forEach(organizationUserMappingResponse -> {
            String code = generateVerificationToken(16);
            String transaction = generateVerificationToken(10);
            String verificationLink = generateVerificationLink(baseUrl, code, transaction).build().toUriString();
            EmailVerification emailVerification = EmailVerification.builder()
                    .organizationId(organizationUserMappingResponse.getOrganizationUserMapping().getOrganizationId())
                    .userId(organizationUserMappingResponse.getUser().getUserId())
                    .transactionId(transaction)
                    .verificationCode(code)
                    .expiresAt(calculateExpirationTime())
                    .build();
            emailVerificationRepository.save(emailVerification);
            sendByTemplateAndUserIds(TemplateEmailReqModel.builder().emailVerifyLink(verificationLink).templateCode("Email_verify")
                    .organizationCode(organizationRepository.findCodeById(organizationUserMappingResponse.getOrganizationUserMapping().getOrganizationId())).userIds(List.of(organizationUserMappingResponse.getUser().getUserId())).build(), null);
        });
    }

    private EmailTemplate replaceJavaTags(EmailTemplate template, TemplateOrganizationEmailReqModel templateOrganizationEmailReqModel) {
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getDocUrl()) && template.getBodyHtml().contains("<java>{{doc.link}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{doc.link}}</java>", templateOrganizationEmailReqModel.getDocUrl());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getConfirmUrl()) && template.getBodyHtml().contains("<java>{{confirm.url}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{confirm.url}}</java>", templateOrganizationEmailReqModel.getConfirmUrl());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganizationCode()) && template.getBodyHtml().contains("<java>{{organization.code}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{organization.code}}</java>", templateOrganizationEmailReqModel.getOrganizationCode());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getEmailVerifyLink()) && template.getBodyHtml().contains("<java>{{verification.link}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{verification.link}}</java>", templateOrganizationEmailReqModel.getEmailVerifyLink());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getMeetingLink()) && template.getBodyHtml().contains("<java>{{meeting.link}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{meeting.link}}</java>", templateOrganizationEmailReqModel.getMeetingLink());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getCalendarLink()) && template.getBodyHtml().contains("<java>{{calendar.link}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{calendar.link}}</java>", templateOrganizationEmailReqModel.getCalendarLink());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getSubscriptionBenefits()) && template.getBodyHtml().contains("<java>{{subscription.benefits}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{subscription.benefits}}</java>", templateOrganizationEmailReqModel.getSubscriptionBenefits());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getSubscriptionName()) && template.getBodyHtml().contains("<java>{{subscription.name}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{subscription.name}}</java>", templateOrganizationEmailReqModel.getSubscriptionName());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getSubscriptionPrice()) && template.getBodyHtml().contains("<java>{{subscription.price}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{subscription.price}}</java>", String.valueOf(templateOrganizationEmailReqModel.getSubscriptionPrice()));
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getSubscriptionRenewal()) && template.getBodyHtml().contains("<java>{{subscription.renewal}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{subscription.renewal}}</java>", String.valueOf(templateOrganizationEmailReqModel.getSubscriptionRenewal()));
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getSubscriptionRenewal()) && template.getBodyHtml().contains("<java>{{subscription.expiration}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{subscription.expiration}}</java>", String.valueOf(templateOrganizationEmailReqModel.getSubscriptionRenewal()));
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getPartnerOrganizationName()) && template.getBodyHtml().contains("<java>{{partner.organization.name}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{partner.organization.name}}</java>", templateOrganizationEmailReqModel.getPartnerOrganizationName());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getPartnershipInitiationTime()) && template.getBodyHtml().contains("<java>{{partnership.initiated.time}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{partnership.initiated.time}}</java>", String.valueOf(templateOrganizationEmailReqModel.getPartnershipInitiationTime()));
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getPartnershipAcceptTime()) && template.getBodyHtml().contains("<java>{{partnership.accept.time}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{partnership.accept.time}}</java>", String.valueOf(templateOrganizationEmailReqModel.getPartnershipAcceptTime()));
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getPartnershipExpirationTime()) && template.getBodyHtml().contains("<java>{{partnership.expiration.time}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{partnership.expiration.time}}</java>", String.valueOf(templateOrganizationEmailReqModel.getPartnershipExpirationTime()));
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganizationCount()) && template.getBodyHtml().contains("<java>{{organization.count}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{organization.count}}</java>", String.valueOf(templateOrganizationEmailReqModel.getOrganizationCount()));
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganization1Name()) && template.getBodyHtml().contains("<java>{{startupA.name}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{startupA.name}}</java>", templateOrganizationEmailReqModel.getOrganization1Name());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganization2Name()) && template.getBodyHtml().contains("<java>{{startupB.name}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{startupB.name}}</java>", templateOrganizationEmailReqModel.getOrganization2Name());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganization1Desc()) && template.getBodyHtml().contains("<java>{{startupA.description}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{startupA.description}}</java>", templateOrganizationEmailReqModel.getOrganization1Desc());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganization2Desc()) && template.getBodyHtml().contains("<java>{{startupB.description}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{startupB.description}}</java>", templateOrganizationEmailReqModel.getOrganization2Desc());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganization1Logo()) && template.getBodyHtml().contains("<java>{{startupA.logo}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{startupA.logo}}</java>", templateOrganizationEmailReqModel.getOrganization1Logo());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganization2Logo()) && template.getBodyHtml().contains("<java>{{startupB.logo}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{startupB.logo}}</java>", templateOrganizationEmailReqModel.getOrganization2Logo());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getFollowerOrganizationName()) && template.getBodyHtml().contains("<java>{{follower.name}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{follower.name}}</java>", templateOrganizationEmailReqModel.getFollowerOrganizationName());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getFollowingOrganizationName()) && template.getBodyHtml().contains("<java>{{following.name}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{following.name}}</java>", templateOrganizationEmailReqModel.getFollowingOrganizationName());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getFollowingDate()) && template.getBodyHtml().contains("<java>{{following.date}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{following.date}}</java>", String.valueOf(templateOrganizationEmailReqModel.getFollowingDate()));
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getEmailUnsubscribeLink()) && template.getBodyHtml().contains("<java>{{unsubscribe.link}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{unsubscribe.link}}</java>", templateOrganizationEmailReqModel.getEmailUnsubscribeLink());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganizationName()) && template.getBodyHtml().contains("<java>{{organization.name}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{organization.name}}</java>", templateOrganizationEmailReqModel.getOrganizationName());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getSenderOrganizationName()) && template.getBodyHtml().contains("<java>{{organization_sender.name}}")) {
            var x = template.getBodyHtml().replace("<java>{{organization_sender.name}}", templateOrganizationEmailReqModel.getSenderOrganizationName());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getMeetingTime()) && template.getBodyHtml().contains("<java>{{meeting.date_time}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{meeting.date_time}}</java>", templateOrganizationEmailReqModel.getMeetingTime());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getMessage()) && template.getBodyHtml().contains("<java>{{organization.message}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{organization.message}}</java>", templateOrganizationEmailReqModel.getMessage());
            template.setBodyHtml(x);
        }
        if (template.getBodyHtml().contains("<java>{{user.name}}</java>")) {
            Organization organization = organizationRepository.findByCode(templateOrganizationEmailReqModel.getOrganizationCode()).get();
            if(Objects.nonNull(organization)) {
                String name =  organizationRepository.findNameById(organization.getId());
                var x = template.getBodyHtml().replace("<java>{{user.name}}</java>", name);
                template.setBodyHtml(x);
            }
        }
        return template;
    }

    private EmailTemplate replaceJavaTagsUser(EmailTemplate template, TemplateEmailReqModel templateOrganizationEmailReqModel) {
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getEmailVerifyLink()) && template.getBodyHtml().contains("<java>{{verification.link}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{verification.link}}</java>", templateOrganizationEmailReqModel.getEmailVerifyLink());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganizationCode()) && template.getBodyHtml().contains("<java>{{organization.code}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{organization.code}}</java>", templateOrganizationEmailReqModel.getOrganizationCode());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganization1Name()) && template.getBodyHtml().contains("<java>{{startupA.name}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{startupA.name}}</java>", templateOrganizationEmailReqModel.getOrganization1Name());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganization2Name()) && template.getBodyHtml().contains("<java>{{startupB.name}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{startupB.name}}</java>", templateOrganizationEmailReqModel.getOrganization2Name());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganization1Desc()) && template.getBodyHtml().contains("<java>{{startupA.description}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{startupA.description}}</java>", templateOrganizationEmailReqModel.getOrganization1Desc());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganization2Desc()) && template.getBodyHtml().contains("<java>{{startupB.description}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{startupB.description}}</java>", templateOrganizationEmailReqModel.getOrganization2Desc());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganization1Logo()) && template.getBodyHtml().contains("<java>{{startupA.logo}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{startupA.logo}}</java>", templateOrganizationEmailReqModel.getOrganization1Logo());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getOrganization2Logo()) && template.getBodyHtml().contains("<java>{{startupB.logo}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{startupB.logo}}</java>", templateOrganizationEmailReqModel.getOrganization2Logo());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getFollowerOrganizationName()) && template.getBodyHtml().contains("<java>{{follower.name}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{follower.name}}</java>", templateOrganizationEmailReqModel.getFollowerOrganizationName());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getUsername()) && template.getBodyHtml().contains("<java>{{user.username}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{user.username}}</java>", templateOrganizationEmailReqModel.getUsername());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getFollowingOrganizationName()) && template.getBodyHtml().contains("<java>{{following.name}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{following.name}}</java>", templateOrganizationEmailReqModel.getFollowingOrganizationName());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getFollowingDate()) && template.getBodyHtml().contains("<java>{{following.date}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{following.date}}</java>", templateOrganizationEmailReqModel.getFollowingDate().toString());
            template.setBodyHtml(x);
        }
        if (!ObjectUtils.isEmpty(templateOrganizationEmailReqModel.getEmailUnsubscribeLink()) && template.getBodyHtml().contains("<java>{{unsubscribe.link}}</java>")) {
            var x = template.getBodyHtml().replace("<java>{{unsubscribe.link}}</java>", templateOrganizationEmailReqModel.getEmailUnsubscribeLink());
            template.setBodyHtml(x);
        }
        return template;
    }

    public void sendByEmail(String templateCode, String email, String verifyLink, String name) {
        try {
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            if (template.getBodyHtml().contains("<java>{{verification.link}}</java>")) {
                var x = template.getBodyHtml().replace("<java>{{verification.link}}</java>", verifyLink);
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{user.name}}</java>")) {
                var x = template.getBodyHtml().replace("<java>{{user.name}}</java>", name);
                template.setBodyHtml(x);
            }
            if (null == template.getSender() || template.getSender().isEmpty()) {
                template.setSender(defaultSender);
            }
            sendInviteEmail(email, template, templateCode);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while preparing emails from template " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error while preparing emails from template " + e.getMessage());
        }
    }

    public void sendPublicProfile(String templateCode, EvaluationEntity entity) {
        try {
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            template.setBodyHtml(replaceBooleanIcons(template.getBodyHtml(), entity));
            template.setBodyHtml(replacePercentageValues(template.getBodyHtml(), entity));
            if (null == template.getSender() || template.getSender().isEmpty()) {
                template.setSender(defaultSender);
            }
            sendInviteEmail(entity.getEmail(), template, templateCode);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while preparing emails from template " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error while preparing emails from template " + e.getMessage());
        }
    }

    String getIcon(boolean value) {
        return value
                ? "https://storage.googleapis.com/sharkdom_resources/tick_circle.png"
                : "https://storage.googleapis.com/sharkdom_resources/cross_circle.png";
    }

    String replaceBooleanIcons(String html, EvaluationEntity entity) {
        html = html.replace(
                "<java>{{booleanEvaluations.is_potential_lead_customer_icon}}</java>",
                getIcon(
                        entity.getBooleanEvaluations().stream()
                                .filter(booleanEvaluation -> booleanEvaluation.getKey().equalsIgnoreCase("is_potential_lead_customer"))
                                .findFirst()
                                .map(BooleanEvaluation::getValue)
                                .orElse(false) // Default to false if not found
                ));
        html = html.replace(
                "<java>{{booleanEvaluations.can_be_marketing_ally_icon}}</java>",
                getIcon(
                        entity.getBooleanEvaluations().stream()
                                .filter(booleanEvaluation -> booleanEvaluation.getKey().equalsIgnoreCase("can_be_marketing_ally"))
                                .findFirst()
                                .map(BooleanEvaluation::getValue)
                                .orElse(false) // Default to false if not found
                ));
        html = html.replace(
                "<java>{{booleanEvaluations.can_be_used_to_onboard_new_users_icon}}</java>",
                getIcon(
                        entity.getBooleanEvaluations().stream()
                                .filter(booleanEvaluation -> booleanEvaluation.getKey().equalsIgnoreCase("can_be_used_to_onboard_new_users"))
                                .findFirst()
                                .map(BooleanEvaluation::getValue)
                                .orElse(false) // Default to false if not found
                ));
        html = html.replace(
                "<java>{{booleanEvaluations.can_help_in_improving_our_brands_image_icon}}</java>",
                getIcon(
                        entity.getBooleanEvaluations().stream()
                                .filter(booleanEvaluation -> booleanEvaluation.getKey().equalsIgnoreCase("can_help_in_improving_our_brands_image"))
                                .findFirst()
                                .map(BooleanEvaluation::getValue)
                                .orElse(false) // Default to false if not found
                ));
        html = html.replace(
                "<java>{{booleanEvaluations.can_help_my_customers_icon}}</java>",
                getIcon(
                        entity.getBooleanEvaluations().stream()
                                .filter(booleanEvaluation -> booleanEvaluation.getKey().equalsIgnoreCase("can_help_my_customers"))
                                .findFirst()
                                .map(BooleanEvaluation::getValue)
                                .orElse(false) // Default to false if not found
                ));
        return html;
    }

    String replacePercentageValues(String html, EvaluationEntity entity) {
        html = html.replace(
                "<java>{{percentageEvaluations.technology_percentage}}</java>",
                entity.getPercentageEvaluations().stream()
                        .filter(percentageEvaluation -> percentageEvaluation.getType().equalsIgnoreCase("TECHNOLOGY"))
                        .findFirst()
                        .map(percentageEvaluation -> String.valueOf(percentageEvaluation.getPercentage()))
                        .orElse("0")
        );
        html = html.replace("<java>{{percentageEvaluations.strategic_percentage}}</java>", entity.getPercentageEvaluations().stream()
                .filter(percentageEvaluation -> percentageEvaluation.getType().equalsIgnoreCase("STRATEGIC"))
                .findFirst()
                .map(percentageEvaluation -> String.valueOf(percentageEvaluation.getPercentage()))
                .orElse("0")
        );
        html = html.replace("<java>{{percentageEvaluations.api_integration_percentage}}</java>", entity.getPercentageEvaluations().stream()
                .filter(percentageEvaluation -> percentageEvaluation.getType().equalsIgnoreCase("API_INTEGRATION"))
                .findFirst()
                .map(percentageEvaluation -> String.valueOf(percentageEvaluation.getPercentage()))
                .orElse("0")
        );
        html = html.replace("<java>{{percentageEvaluations.co_marketing_percentage}}</java>", entity.getPercentageEvaluations().stream()
                .filter(percentageEvaluation -> percentageEvaluation.getType().equalsIgnoreCase("CO-MARKETING"))
                .findFirst()
                .map(percentageEvaluation -> String.valueOf(percentageEvaluation.getPercentage()))
                .orElse("0")
        );
        html = html.replace("<java>{{percentageEvaluations.sales_percentage}}</java>", entity.getPercentageEvaluations().stream()
                .filter(percentageEvaluation -> percentageEvaluation.getType().equalsIgnoreCase("SALES"))
                .findFirst()
                .map(percentageEvaluation -> String.valueOf(percentageEvaluation.getPercentage()))
                .orElse("0")
        );
        html = html.replace("<java>{{percentageEvaluations.marketing_percentage}}</java>", entity.getPercentageEvaluations().stream()
                .filter(percentageEvaluation -> percentageEvaluation.getType().equalsIgnoreCase("MARKETING"))
                .findFirst()
                .map(percentageEvaluation -> String.valueOf(percentageEvaluation.getPercentage()))
                .orElse("0")
        );
        html = html.replace("<java>{{percentageEvaluations.brand_licensing_percentage}}</java>", entity.getPercentageEvaluations().stream()
                .filter(percentageEvaluation -> percentageEvaluation.getType().equalsIgnoreCase("BRAND_LICENSING"))
                .findFirst()
                .map(percentageEvaluation -> String.valueOf(percentageEvaluation.getPercentage()))
                .orElse("0")
        );
        return html;
    }

    public void invitePartner(String templateCode, String email, String verifyLink, String message, Long organizationId) {
        try {
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            if (template.getBodyHtml().contains("<java>{{verification.link}}</java>")) {
                var x = template.getBodyHtml().replace("<java>{{verification.link}}</java>", verifyLink);
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{user.message}}</java>")) {
                var x = template.getBodyHtml().replace("<java>{{user.message}}</java>", message);
                template.setBodyHtml(x);
            }
            String senderName = organizationRepository.findNameById(organizationId);
            senderName = senderName.replace(" ", ".");
            String senderEmail = String.format("%s@sharkdom.com", senderName);
            if (!senderName.isEmpty()) {
                template.setSender(String.format("%s via SharkDom <%s>", senderName, senderEmail));
            }
            if (null == template.getSender() || template.getSender().isEmpty()) {
                template.setSender(defaultSender);
            }
            sendInviteEmail(email, template, templateCode);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while preparing emails from template " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error while preparing emails from template " + e.getMessage());
        }
    }

    private void sendInviteEmail(String email, EmailTemplate template, String templateCode) {
        EmailReqModelWithMultipartAttachments emailReqModelWithMultipartAttachments = new EmailReqModelWithMultipartAttachments();
        emailReqModelWithMultipartAttachments.setRecipients(List.of(email));
        emailReqModelWithMultipartAttachments.setBodyHtml(template.getBodyHtml());
        emailReqModelWithMultipartAttachments.setSubject(template.getSubject());
        emailReqModelWithMultipartAttachments.setBodyText(template.getBodyString());
        emailReqModelWithMultipartAttachments.setFrom(template.getSender());
        sendMultiple(List.of(emailReqModelWithMultipartAttachments), templateCode, 1L, 1L);
    }

    @Transactional
    public List<EmailReqModelWithResponse> resetPassword(String email, String link) {
        return null;
    }

    public DomainVerificationResponse createDomainIdentity(Long organizationId, String domain, String email) {

        var org = organizationRepository.findById(organizationId);
        if (org.isEmpty()) {
            throw new SharkdomException(ErrorMessages.SH22, organizationId);
        }

        var existing = domainIdentityRepository.findByOrganizationId(organizationId);
        if (existing.isPresent()) {
            throw new SharkdomException(ErrorMessages.SH103, organizationId);
        }
        var response = sesDomainIdentityService.createDomainIdentity(domain);
        String dnsRecordsJson = null;
        try {
            dnsRecordsJson = objectMapper.writeValueAsString(response.getDnsRecords());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        DomainIdentity domainIdentity = DomainIdentity.builder()
                .dnsRecords(dnsRecordsJson).email(email).organizationId(organizationId).domain(domain).build();
        domainIdentityRepository.save(domainIdentity);
        return response;

    }

    public DomainVerificationResponse getStatus(String domain) {
        return sesDomainIdentityService.getDomainStatus(domain);
    }

    public DomainResponse getDomainDetails(Long organizationId) {
        try {
            var optionalDomainIdentity = domainIdentityRepository.findByOrganizationId(organizationId);
            if (optionalDomainIdentity.isPresent()) {
                var domainIdentity = optionalDomainIdentity.get();
                DomainVerificationResponse.DnsRecords dnsRecords = objectMapper.readValue(domainIdentity.getDnsRecords(), DomainVerificationResponse.DnsRecords.class);
                return DomainResponse.builder().domain(domainIdentity.getDomain())
                        .email(domainIdentity.getEmail())
                        .dnsRecords(dnsRecords)
                        .isEmailVerified(domainIdentity.isEmailVerified())
                        .isVerified(domainIdentity.isVerified()).build();
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void updateDomainVerificationStatus() {
        List<DomainIdentity> domains = domainIdentityRepository.findAllPendingVerification();

        for (DomainIdentity domain : domains) {
            try {
                DomainVerificationResponse response = sesDomainIdentityService.getDomainStatus(domain.getDomain());
                if (response.getStatus().contains("Domain Verification: Success")) {
                    domain.setVerified(true);
                    domainIdentityRepository.save(domain);
                    String code = generateVerificationToken(16);
                    String transaction = generateVerificationToken(10);
                    String verificationLink = generateVerificationLink(baseUrl, code, transaction).build().toUriString();
                    EmailVerification emailVerification = EmailVerification.builder()
                            .organizationId(domain.getOrganizationId())
                            .userId("UserId")
                            .email(domain.getEmail())
                            .transactionId(transaction)
                            .verificationCode(code)
                            .expiresAt(calculateExpirationTime())
                            .build();
                    emailVerificationRepository.save(emailVerification);
                    var templateModel = TemplateEmailReqModel.builder().emailVerifyLink(verificationLink).templateCode("Email_verify").build();
                    EmailTemplate firebaseTemplate = emailFromTemplateService.getTemplateByCode(templateModel.getTemplateCode());
                    EmailTemplate template = replaceJavaTagsUser(firebaseTemplate, templateModel);

                    if (null == template.getSender() || template.getSender().isEmpty()) {
                        template.setSender(defaultSender);
                    }
                    var mailEntity = new EmailReqModelWithMultipartAttachments();
                    String templateCode = mailEntity.getBodyHtml();
                    mailEntity.setBodyHtml(template.getBodyHtml());
                    mailEntity.setSubject(template.getSubject());
                    mailEntity.setBodyText(template.getBodyString());
                    mailEntity.setFrom(template.getSender());
                    mailEntity.setRecipients(Collections.singletonList(domain.getEmail()));
                    new EmailReqModelWithResponse(mailEntity, send(mailEntity, templateCode, 1L, 1L));
                }
            } catch (Exception e) {
            }
        }
    }

//    public void sendOtpByEmail(String key, String correctOTP, String templateCode) throws Exception {
//        try {
//            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
//            String x = template.getBodyHtml().replace("<java>{{correctOTP}}</java>", correctOTP);
//            String subject = template.getSubject()
//                    .replace("<java>{{correctOTP}}</java>", correctOTP);
//            template.setBodyHtml(x);
//            amazonSes.prepareAndSend(
//                    template.getSubject(),
//                    x,
//                    List.of(),
//                    template.getSender() != null ? template.getSender() : defaultSender,
//                    key,
//                    List.of(),
//                    "email_verify_otp",
//                    0L
//            );
//            log.info("OTP sent successfully to {}", key);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }

    public void sendOtpByEmail(String key, String correctOTP, String templateCode) throws Exception {
        try {

            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);

            String body = template.getBodyHtml()
                    .replace("<java>{{correctOTP}}</java>", correctOTP);

            String subject = template.getSubject().replace("{{otp}}", correctOTP);

            amazonSes.prepareAndSend(
                    subject,
                    body,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    key,
                    List.of(),
                    "email_verify_otp",
                    0L
            );

            log.info("OTP sent successfully to {}", key);

        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public void sendEmailForStripePaymentMode(String templateCode, String organizationName, StripeCheckoutSessions stripeCheckoutSessions) throws Exception {
        try {
            StripePlanType stripeCheckoutSessionPlanType = stripeCheckoutSessions.getLineItems().get(0).getPrice().getPlanType();
            StripePlanConfiguration stripePlanConfiguration = stripePlanConfigurationRepository.findById(stripeCheckoutSessionPlanType)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH16, stripeCheckoutSessionPlanType));
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String x;
            if (template.getBodyHtml().contains("<java>{{subscription.name}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.name}}</java>", stripePlanConfiguration.getPlanType().getPlanName());
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{subscription.benefits}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.benefits}}</java>", "http://doc.sharkdom.com/pricing");
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{subscription.price}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.price}}</java>", String.valueOf(stripePlanConfiguration.getAmount()));
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{subscription.renewal}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.renewal}}</java>", null);
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{organization.name}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{organization.name}}</java>", organizationName);
                template.setBodyHtml(x);
            }
            x = template.getBodyHtml();
            amazonSes.prepareAndSend(
                    template.getSubject(),
                    x,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    stripeCheckoutSessions.getCustomer().getCustomerEmail(),
                    List.of(),
                    templateCode,
                    0L
            );
            log.info("Payment mail sent successfully to {}", stripeCheckoutSessions.getCustomer().getCustomerEmail());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public void sendEmailForStripeSubscriptionMode(String templateCode, String organizationName, StripeSubscriptionData stripeSubscriptionData, InputStream downloadPdfAsInputStream) throws Exception {
        try {
            StripePlanType stripeSubscriptionPlanType = stripeSubscriptionData.getPrice().getPlanType();
            StripePlanConfiguration stripePlanConfiguration = stripePlanConfigurationRepository.findById(stripeSubscriptionPlanType)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH16, stripeSubscriptionPlanType));
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String x;
            if (template.getBodyHtml().contains("<java>{{subscription.name}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.name}}</java>", stripeSubscriptionData.getPrice().getPlanType().getPlanName());
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{subscription.benefits}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.benefits}}</java>", "http://doc.sharkdom.com/pricing");
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{subscription.price}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.price}}</java>", String.valueOf(stripePlanConfiguration.getAmount()));
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{subscription.renewal}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.renewal}}</java>", String.valueOf(stripeSubscriptionData.getEndOn()));
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{organization.name}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{organization.name}}</java>", organizationName);
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{subscription.trialendOn}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.trialendOn}}</java>", String.valueOf(stripeSubscriptionData.getEndOn()));
                template.setBodyHtml(x);
            }
            x = template.getBodyHtml();
            amazonSes.prepareAndSendInvoice(
                    template.getSubject(),
                    x,
                    generateInvoiceFilename(stripeSubscriptionData),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    stripeSubscriptionData.getCustomer().getCustomerEmail(),
                    downloadPdfAsInputStream,
                    templateCode,
                    0L
            );
            log.info("Subscription mail sent successfully to {}", stripeSubscriptionData.getCustomer().getCustomerEmail());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public void sendEmailForSubscriptionStatus(String templateCode, String organizationName, StripeSubscriptionData stripeSubscriptionData) throws Exception {
        try {
            log.info("Template code using schedulers: {}", templateCode);
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String x;
            if (template.getBodyHtml().contains("<java>{{subscription.name}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.name}}</java>", stripeSubscriptionData.getPrice().getPlanType().getPlanName());
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{subscription.expiration}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.expiration}}</java>", String.valueOf(LocalDate.now()));
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{organization.name}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{organization.name}}</java>", organizationName);
                template.setBodyHtml(x);
            }
            x = template.getBodyHtml();
            amazonSes.prepareAndSend(
                    template.getSubject(),
                    x,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    stripeSubscriptionData.getCustomer().getCustomerEmail(),
                    List.of(),
                    templateCode,
                    0L
            );
            log.info("Subscription status mail sent successfully to {}", stripeSubscriptionData.getCustomer().getCustomerEmail());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public void sendEmailForUpgradeStripeSubscription(String templateCode, String organizationName, StripeSubscriptionData stripeSubscriptionData, InputStream downloadPdfAsInputStream) throws Exception {
        try {
            StripePlanType stripeSubscriptionPlanType = stripeSubscriptionData.getPrice().getPlanType();
            StripePlanConfiguration stripePlanConfiguration = stripePlanConfigurationRepository.findById(stripeSubscriptionPlanType)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH16, stripeSubscriptionPlanType));
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String x;
            if (template.getBodyHtml().contains("<java>{{subscription.name}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.name}}</java>", stripeSubscriptionData.getPrice().getPlanType().getPlanName());
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{subscription.benefits}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.benefits}}</java>", "http://doc.sharkdom.com/pricing");
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{subscription.price}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.price}}</java>", String.valueOf(stripePlanConfiguration.getAmount()));
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{subscription.renewal}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.renewal}}</java>", String.valueOf(stripeSubscriptionData.getEndOn()));
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{organization.name}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{organization.name}}</java>", organizationName);
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{subscription.trialendOn}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{subscription.trialendOn}}</java>", String.valueOf(stripeSubscriptionData.getEndOn()));
                template.setBodyHtml(x);
            }
            x = template.getBodyHtml();
            amazonSes.prepareAndSendInvoice(
                    template.getSubject(),
                    x,
                    generateInvoiceFilename(stripeSubscriptionData),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    stripeSubscriptionData.getCustomer().getCustomerEmail(),
                    downloadPdfAsInputStream,
                    templateCode,
                    0L
            );
            log.info("Subscription change mail sent successfully to {}", stripeSubscriptionData.getCustomer().getCustomerEmail());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public void sendEmailForSubscriptionAutoRenewalFailed(String templateCode, String organizationName, Invoice invoice) throws Exception {
        try {
            log.info("Template code using schedulers: {}", templateCode);
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String x;
            if (template.getBodyHtml().contains("<java>{{invoice.currency}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{invoice.currency}}</java>", invoice.getCurrency());
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{invoice.amountDue}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{invoice.amountDue}}</java>", String.valueOf(invoice.getAmountDue()));
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{organization.name}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{organization.name}}</java>", organizationName);
                template.setBodyHtml(x);
            }
            x = template.getBodyHtml();
            amazonSes.prepareAndSend(
                    template.getSubject(),
                    x,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    invoice.getCustomerEmail(),
                    List.of(),
                    templateCode,
                    0L
            );
            log.info("Subscription status mail sent successfully to {}", invoice.getCustomerEmail());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public void inviteDiscord(Long partnerOrganizationId, String discordLink) throws Exception {
        try {
            var organization = organizationRepository.findById(partnerOrganizationId);
            if (organization.isEmpty()) {
                throw new ServiceException(ErrorMessages.SH22, partnerOrganizationId);
            }
            var templateCode = "discord_user_invite";
            log.info("Template code using schedulers: {}", templateCode);
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String x;
            if (template.getBodyHtml().contains("<java>{{discord.code}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{discord.code}}</java>", discordLink);
                template.setBodyHtml(x);
            }
            if (template.getBodyHtml().contains("<java>{{organization.name}}</java>")) {
                x = template.getBodyHtml().replace("<java>{{organization.name}}</java>", organization.get().getName());
                template.setBodyHtml(x);
            }
            x = template.getBodyHtml();
            amazonSes.prepareAndSend(
                    template.getSubject(),
                    x,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    organization.get().getPrimaryEmail(),
                    List.of(),
                    templateCode,
                    0L
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    private String generateInvoiceFilename(StripeSubscriptionData stripeSubscriptionData) {
        return String.format("%s_%s_%s_%s_%s.pdf",
                "SHARKDOM", // Your business name
                stripeSubscriptionData.getPrice().getPlanType().getPlanName().replace(" ", "_"),
                LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                stripeSubscriptionData.getLatestInvoice().substring(4, 10), // Shorten inv_abc123xyz to abc123
                stripeSubscriptionData.getCustomer().getCustomerId().substring(4, 10) // Shorten cus_def456 to def456
        );
    }
//    public EmailForwardReqModelWithResponse forwardEmail(ForwardEmailRequestNew request)
//            throws ExecutionException, InterruptedException {
//
////        String fromEmail = request.getFromEmail();
//
//        User user = userRepository.findByUserId(request.getUserId())
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        ErrorMessages.valueOf("User not found for ID " + request.getUserId())
//                ));
//
//        Long orgId = Util.getOrgIdFromToken();
//        Organization org = organizationRepository.findById(orgId)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        ErrorMessages.valueOf("Organization not found for ID " + orgId)
//                ));
//
//        String toEmailOrg = formatEmail(org.getName(), "sharkdom.com");
//        String toEmailUser = formatEmail(user.getName(), "sharkdom.com");
//
//        EmailTemplate template = emailFromTemplateService.getTemplateByCode(request.getTemplateCode());
//
////        if (template.getSender() == null || template.getSender().isEmpty()) {
////            fromEmail = defaultSender; // fallback to default sender
////        } else {
////            fromEmail = template.getSender();
////        }
//
//        String subject = template.getSubject();
//        String bodyHtml = template.getBodyHtml();
////        String bodyText = template.getBodyString();
//
//        if (bodyHtml.contains("<java>{{unsubscribe.link}}</java>")) {
//            String unsubscribeLink = "https://sharkdom.com/unsubscribe?email=" + user.getEmail();
//            bodyHtml = bodyHtml.replace("<java>{{unsubscribe.link}}</java>", unsubscribeLink);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//
//        try {
//            // First try organization email
//            amazonSes.prepareAndSend(subject, bodyHtml, null, fromEmail, toEmailOrg, null, request.getTemplateCode(), null);
//
//            return saveEmailStats(uuid, fromEmail, orgId, toEmailOrg, null, bodyHtml, "SENT",
//                    "Email sent to organization successfully");
//
//        } catch (Exception orgEx) {
//            log.warn("Email sending to org failed, retrying with user. OrgId={}, UserId={}", orgId, request.getUserId(), orgEx);
//
//            try {
//                // Fallback to user email
//                amazonSes.prepareAndSend(subject, bodyHtml, null, fromEmail, toEmailUser, null, request.getTemplateCode(), null);
//
//                return saveEmailStats(uuid, fromEmail, orgId, toEmailUser, null, bodyHtml, "SENT",
//                        "Email sent to user successfully");
//
//            } catch (Exception userEx) {
//                log.error("Email sending failed for userId={}", request.getUserId(), userEx);
//
//                saveEmailStats(uuid, fromEmail, orgId, toEmailUser, null, bodyHtml, "FAILED", userEx.getMessage());
//
//                throw new ServiceException(ErrorMessages.SH116,
//                        "Email sending failed to both organization and user", userEx);
//            }
//        }
//    }
public String replyToOriginalSender(ForwardEmailRequestNew request) {
    try {
        String toEmail = request.getToEmail();
        String replyBodyHtml = request.getReplyBodyHtml();

        if (toEmail == null || replyBodyHtml == null) {
            throw new IllegalArgumentException("Recipient email and reply body cannot be null");
        }

        // Fetch original email details
        EmailForwardStats originalEmail = emailStatsRepository
                .findTopBySenderEmailOrderByCreatedAtDesc(toEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.valueOf("No email found with sender " + toEmail)
                ));

        String from = originalEmail.getReceiverEmail();

        if (from == null || !from.contains("@")) {
            throw new IllegalStateException("Invalid 'from' email address: " + from);
        }

        String subject = extractSubject(replyBodyHtml);
        String domain = from.substring(from.indexOf("@") + 1);

        DomainVerificationResponse statusResponse;
        try {
            statusResponse = sesDomainIdentityService.getDomainStatus(domain);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch SES domain status for " + domain, e);
        }

        if (statusResponse == null || !statusResponse.getStatus().contains("Success")) {
            try {
                DomainVerificationResponse verificationResponse =
                        sesDomainIdentityService.createDomainIdentity(domain);

                return "Domain " + domain + " is not verified. Please add the following DNS records:\n"
                        + "TXT: " + verificationResponse.getDnsRecords().getVerificationRecord().getName()
                        + " = " + verificationResponse.getDnsRecords().getVerificationRecord().getValue()
                        + "\nDKIM Records: " + verificationResponse.getDnsRecords().getDkimRecords();
            } catch (Exception e) {
                throw new RuntimeException("Failed to initiate SES domain verification for " + domain, e);
            }
        }


        try {
            amazonSes.prepareAndSend(
                    subject,
                    replyBodyHtml,
                    null,
                    from,
                    toEmail,
                    null,
                    null,
                    null
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to send reply from " + from + " to " + toEmail, e);
        }

        return "Reply sent from " + from + " to " + toEmail;

    } catch (ResourceNotFoundException e) {
        return "Error: " + e.getMessage();
    } catch (IllegalArgumentException | IllegalStateException e) {
        return "Validation error: " + e.getMessage();
    } catch (RuntimeException e) {
        // Catch SES + other runtime issues
        return "Processing error: " + e.getMessage();
    } catch (Exception e) {
        // Fallback for any unexpected issue
        return "Unexpected error while replying: " + e.getMessage();
    }
}



    private String formatEmail(String name, String domain) {
        String formattedName = name.trim();
        String formattedEmail = name.trim().toLowerCase().replaceAll("\\s+", ".") + "@" + domain;
        return formattedName + " <" + formattedEmail + ">";
    }

    public EmailForwardReqModelWithResponse saveEmailStats(
            String uuid,
            String senderEmail,
            Long senderOrgId,
            String receiverEmail,
            Long receiverOrgId,
            String body,
            String status,String subject) {

        EmailForwardStats stats = new EmailForwardStats();
        stats.setUuid(uuid);
        stats.setSenderEmail(senderEmail);
        stats.setSenderOrgId(senderOrgId);
        stats.setReceiverEmail(receiverEmail);
        stats.setReceiverOrgId(receiverOrgId);
        stats.setMessageBody(body);
        stats.setStatus(status);
        stats.setSubject(subject);
//        stats.setMessage(message); // only if column exists

        // Persist entity
        EmailForwardStats saved = emailStatsRepository.save(stats);

        // Return using saved entity (guaranteed DB defaults applied)
        return new EmailForwardReqModelWithResponse(
                saved.getUuid(),
                saved.getStatus(),
                saved.getMessageBody()
        );
    }

    public String sendEmailToOrgUsers( ForwardEmailRequestNew request) throws Exception {

        OrganizationUserMapping mapping = organizationUserMappingRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.valueOf("No organization mapping found for user " + request.getUserId())));

        Long orgId = mapping.getOrganizationId();


        List<OrganizationUserMappingResponse> orgUsers = organizationUserMappingRepository.findAllByOrganizationId(orgId);
        if (orgUsers.isEmpty()) {
            throw new ResourceNotFoundException(
                    ErrorMessages.valueOf("No users found in organization " + orgId));
        }

        // 3. Extract subject from <title>
        String subject = extractSubject(request.getReplyBodyHtml());
        String cleanedBody = removeTitle(request.getReplyBodyHtml());

        // 4. Build recipients (comma separated)
        String recipients = orgUsers.stream()
                .map(orgUser -> orgUser.getUser().getEmail())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));


        Organization org = organizationRepository.findById(Util.getOrgIdFromToken())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.valueOf("Organization not found with id " + orgId)));
        String senderEmail = org.getName() + " <" + org.getName().toLowerCase() + "@sharkdom.com>";

        // 6. Call SES wrapper
        amazonSes.prepareAndSend(
                subject,
                cleanedBody,
                null,                     // s3AttachmentNames
                senderEmail,              // from
                recipients,               // all org user emails
                null,                     // multipart attachments
                null,             // custom template code
                null                      // collaborationId (optional)
        );


        for (OrganizationUserMappingResponse orgUser : orgUsers) {
            String receiverEmail = orgUser.getUser().getEmail();

            saveEmailStats(
                    UUID.randomUUID().toString(),
                    senderEmail,
                    Util.getOrgIdFromToken(),
                    receiverEmail,
                    orgId,
                    cleanedBody,
                    "SENT",subject
            );
        }

        return "Email sent to " + orgUsers.size() + " users in organization " + orgId;
    }

    private String extractSubject(String body) {
        Matcher matcher = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
                .matcher(body);
        return matcher.find() ? matcher.group(1).trim() : "Sharkdom Email";
    }

    private String removeTitle(String body) {
        return body.replaceAll("(?i)<title>.*?</title>", "");
    }

    public void sendPartnerCredentialByEmail(
            String receiverEmail,
            String username,
            String password,
            String url,
            String templateCode) throws Exception {
        try {
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);

            // Perform all replacements on the same HTML string
            String body = template.getBodyHtml()
                    .replace("<java>{{username}}</java>", username)
                    .replace("<java>{{password}}</java>", password)
                    .replace("<java>{{url}}</java>", url);

            template.setBodyHtml(body);

            amazonSes.prepareAndSend(
                    template.getSubject(),
                    body,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    receiverEmail,
                    List.of(),
                    "send_Credentials_Partner",
                    0L
            );

            log.info("Credential sent successfully to {}", receiverEmail);
        } catch (Exception e) {
            log.error("Error sending credentials: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void sendByEmailAddTeamMember(String templateCode, String email, String verifyLink, String organizationName) {

        log.info("Send Team Member Invite started | templateCode={} | email={} | organizationName={}",
                templateCode, email, organizationName);

        try {
            log.debug("Fetching email template for code: {}", templateCode);
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);

            if (template == null) {
                log.warn("No email template found for code: {}. Email will not be sent.", templateCode);
                return;
            }

            log.debug("Email template fetched successfully | templateCode={}", templateCode);

            String bodyHtml = template.getBodyHtml();

            if (bodyHtml == null || bodyHtml.isEmpty()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
            } else {
                log.debug("Replacing placeholders in email body");

                if (bodyHtml.contains("<java>{{verification.link}}</java>")) {
                    bodyHtml = bodyHtml.replace("<java>{{verification.link}}</java>", verifyLink);
                    log.debug("Replaced verification link placeholder");
                } else {
                    log.warn("Verification link placeholder NOT found in template");
                }

                if (bodyHtml.contains("<java>{{organization.name}}</java>")) {
                    bodyHtml = bodyHtml.replace("<java>{{organization.name}}</java>", organizationName);
                    log.debug("Replaced organization name placeholder");
                } else {
                    log.warn("Organization name placeholder NOT found in template");
                }

                template.setBodyHtml(bodyHtml);
            }

            if (template.getSender() == null || template.getSender().isEmpty()) {
                template.setSender(defaultSender);
                log.info("Sender email not found in template, using default sender: {}", defaultSender);
            } else {
                log.debug("Using sender from template: {}", template.getSender());
            }

            log.info("Sending invite email to {}", email);
            sendInviteEmail(email, template, templateCode);

            log.info("Invite email sent successfully | to={} | templateCode={}", email, templateCode);

        } catch (Exception e) {
            log.error("Failed to send invite email | templateCode={} | email={} | error={}",
                    templateCode, email, e.getMessage(), e);

            throw new RuntimeException("Failed to send team member invite email", e);
        }
    }

    public void sendBusinessAlertEmail(String templateCode, String email, Map<String, String> variables) {
        try {
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            if (template == null) {
                log.warn("No email template found for code: {}", templateCode);
                return;
            }

            String bodyHtml = template.getBodyHtml();
            if (bodyHtml != null && !bodyHtml.isEmpty()) {

                // Replace all dynamic java variables
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    String placeHolder = "<java>{{" + entry.getKey() + "}}</java>";
                    bodyHtml = bodyHtml.replace(placeHolder, entry.getValue());
                }

                template.setBodyHtml(bodyHtml);
            }

            // Set default sender if not present
            if (template.getSender() == null || template.getSender().isEmpty()) {
                template.setSender(defaultSender);
            }

            sendInviteEmail(email, template, templateCode);
            log.info("Business alert email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Error while sending business email for templateCode {} to {}",
                    templateCode, email, e);
            throw new RuntimeException("Failed to send business alert email", e);
        }
    }

    public void sendLicenseAllocationEmail(String templateCode, String email, Map<String, String> variables) {
        try {
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            if (template == null) {
                log.warn("No email template found for code: {}", templateCode);
                return;
            }
            String bodyHtml = template.getBodyHtml();
            if (bodyHtml != null && !bodyHtml.isEmpty()) {

                // Replace all dynamic java variables
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    String placeHolder = "<java>{{" + entry.getKey() + "}}</java>";
                    bodyHtml = bodyHtml.replace(placeHolder, entry.getValue());
                }

                template.setBodyHtml(bodyHtml);
            }

            // Set default sender if not present
            if (template.getSender() == null || template.getSender().isEmpty()) {
                template.setSender(defaultSender);
            }

            sendInviteEmail(email, template, templateCode);
            log.info("Business alert email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Error while sending business email for templateCode {} to {}",
                    templateCode, email, e);
            throw new RuntimeException("Failed to send business alert email", e);
        }
    }

    public void sendConfirmLoginEmail(
            String templateCode,
            String email,
            String userName,
            String loginTime,
            String location,
            String device,
            String ip,
            String confirmLink
    ) {

        log.info("Send Confirm Login Email started | templateCode={} | email={}",
                templateCode, email);

        try {
            log.debug("Fetching email template for code: {}", templateCode);
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);

            if (template == null) {
                log.warn("No email template found for code: {}. Email will not be sent.", templateCode);
                return;
            }

            log.debug("Email template fetched successfully | templateCode={}", templateCode);

            String bodyHtml = template.getBodyHtml();

            if (bodyHtml == null || bodyHtml.isEmpty()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
            } else {
                log.debug("Replacing placeholders in confirm login email body");

                bodyHtml = replaceIfPresent(bodyHtml, "<java>{{user.name}}</java>", userName, "user name");
                bodyHtml = replaceIfPresent(bodyHtml, "<java>{{login.time}}</java>", loginTime, "login time");
                bodyHtml = replaceIfPresent(bodyHtml, "<java>{{login.location}}</java>", location, "location");
                bodyHtml = replaceIfPresent(bodyHtml, "<java>{{login.device}}</java>", device, "device");
                bodyHtml = replaceIfPresent(bodyHtml, "<java>{{login.ip}}</java>", ip, "IP address");
                bodyHtml = replaceIfPresent(bodyHtml, "<java>{{confirm.link}}</java>", confirmLink, "confirm link");

                template.setBodyHtml(bodyHtml);
            }

            if (template.getSender() == null || template.getSender().isEmpty()) {
                template.setSender(defaultSender);
                log.info("Sender email not found in template, using default sender: {}", defaultSender);
            } else {
                log.debug("Using sender from template: {}", template.getSender());
            }


        } catch (Exception e) {
            log.error("Failed to send confirm login email | templateCode={} | email={} | error={}",
                    templateCode, email, e.getMessage(), e);

            throw new RuntimeException("Failed to send confirm login email", e);
        }
    }

    private String replaceIfPresent(
            String bodyHtml,
            String placeholder,
            String value,
            String logName
    ) {
        if (bodyHtml.contains(placeholder)) {
            log.debug("Replacing {} placeholder", logName);
            return bodyHtml.replace(placeholder, value);
        } else {
            log.warn("{} placeholder NOT found in template", logName);
            return bodyHtml;
        }
    }

    public void sendByEmailBookDemo(
            String templateCode,
            String email,
            String meetingLink,
            String demoDate,
            String demoTime,
            String timeZone,
            String duration
    ) {

        log.info("Send Book Demo Email started | templateCode={} | email={}",
                templateCode, email);

        try {
            log.debug("Fetching email template for code: {}", templateCode);
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);

            if (template == null) {
                log.warn("No email template found for code: {}. Email will not be sent.", templateCode);
                return;
            }

            log.debug("Email template fetched successfully | templateCode={}", templateCode);

            String bodyHtml = template.getBodyHtml();

            if (bodyHtml == null || bodyHtml.isEmpty()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
            } else {
                log.debug("Replacing placeholders in email body");

                // Meeting Link
                if (bodyHtml.contains("{{meetingLink}}")) {
                    bodyHtml = bodyHtml.replace("{{meetingLink}}", meetingLink);
                    log.debug("Replaced meetingLink placeholder");
                } else {
                    log.warn("Meeting link placeholder NOT found in template");
                }

                // Demo Date
                if (bodyHtml.contains("{{demoDate}}")) {
                    bodyHtml = bodyHtml.replace("{{demoDate}}", demoDate);
                    log.debug("Replaced demoDate placeholder");
                } else {
                    log.warn("Demo date placeholder NOT found in template");
                }

                // Demo Time
                if (bodyHtml.contains("{{demoTime}}")) {
                    bodyHtml = bodyHtml.replace("{{demoTime}}", demoTime);
                    log.debug("Replaced demoTime placeholder");
                } else {
                    log.warn("Demo time placeholder NOT found in template");
                }

                // Time Zone
                if (bodyHtml.contains("{{timeZone}}")) {
                    bodyHtml = bodyHtml.replace("{{timeZone}}", timeZone);
                    log.debug("Replaced timeZone placeholder");
                } else {
                    log.warn("Time zone placeholder NOT found in template");
                }

                // Duration
                if (bodyHtml.contains("{{duration}}")) {
                    bodyHtml = bodyHtml.replace("{{duration}}", duration);
                    log.debug("Replaced duration placeholder");
                } else {
                    log.warn("Duration placeholder NOT found in template");
                }

                template.setBodyHtml(bodyHtml);
            }

            if (template.getSender() == null || template.getSender().isEmpty()) {
                template.setSender(defaultSender);
                log.info("Sender email not found in template, using default sender: {}", defaultSender);
            } else {
                log.debug("Using sender from template: {}", template.getSender());
            }

            log.info("Sending Book Demo email to {}", email);
            sendInviteEmail(email, template, templateCode);

            log.info("Book Demo email sent successfully | to={} | templateCode={}", email, templateCode);

        } catch (Exception e) {
            log.error("Failed to send Book Demo email | templateCode={} | email={} | error={}",
                    templateCode, email, e.getMessage(), e);

            throw new RuntimeException("Failed to send book demo email", e);
        }
    }


    public void sendConfirmLoginInvite(
            String templateCode,
            String email,
            String userName,
            String loginTime,
            String location,
            String device,
            String ip
    ) throws Exception {

        try {
            log.info("Template code using schedulers: {}", templateCode);

            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String x = template.getBodyHtml();

            if (x == null || x.isBlank()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
                return;
            }

            // Match real HTML placeholders
            x = x.replace("{{user.name}}", userName != null ? userName : "");
            x = x.replace("{{login.time}}", loginTime != null ? loginTime : "");
            x = x.replace("{{login.location}}", location != null ? location : "");
            x = x.replace("{{login.device}}", device != null ? device : "");
            x = x.replace("{{login.ip}}", ip != null ? ip : "");

            template.setBodyHtml(x);

            amazonSes.prepareAndSend(
                    template.getSubject(),
                    x,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    email,
                    List.of(),
                    templateCode,
                    0L
            );

            log.info("Confirm login email sent successfully | email={}", email);

        } catch (Exception e) {
            log.error("Failed to send confirm login email | templateCode={} | email={} | error={}",
                    templateCode, email, e.getMessage(), e);
            throw e;
        }
    }

    public void sendTalentNetworkCreatedEmail(
            String templateCode,
            TalentNetwork tn,
            String email
    ) throws Exception {

        try {
            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String body = template.getBodyHtml();

            if (body == null || body.isBlank()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
                return;
            }

            body = body.replace("{{company.name}}", safe(tn.getCompanyName()));
            body = body.replace("{{company.website}}", safe(tn.getWebsiteUrl()));
            body = body.replace("{{contact.email}}", safe(tn.getContactEmail()));
            body = body.replace("{{contact.phone}}", safe(tn.getContactPhoneNumber()));
            body = body.replace("{{job.title}}", safe(tn.getJobTitle()));
            body = body.replace("{{job.location}}", safe(tn.getPreferredLocation()));
            body = body.replace("{{job.experience}}",
                    tn.getMinimumYearsOfExperience() != null ?
                            String.valueOf(tn.getMinimumYearsOfExperience()) : "0");
            body = body.replace("{{job.summary}}", safe(tn.getRoleSummary()));
            body = body.replace("{{company.linkedin}}", safe(tn.getLinkedinUrl()));
            body = body.replace("{{screening.bot}}",
                    Boolean.TRUE.equals(tn.getUseScreeningBot()) ? "Yes" : "No");
            body = body.replace("{{response.time}}", safe(tn.getResponseTime()));

            amazonSes.prepareAndSend(
                    template.getSubject(),
                    body,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    email,
                    List.of(),
                    templateCode,
                    0L
            );

            log.info("Talent network email sent successfully | email={}", tn.getContactEmail());

        } catch (Exception e) {
            log.error("Failed to send talent network email | error={}", e.getMessage(), e);
            throw e;
        }
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    public void sendTemplateWithUserName(
            String templateCode,
            String email,
            String userName
    ) throws Exception {

        try {
            log.info("Sending email using templateCode={} to email={}", templateCode, email);

            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String body = template.getBodyHtml();

            if (body == null || body.isBlank()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
                return;
            }

            // Replace placeholder
            body = body.replace("{{user.name}}",
                    userName != null ? userName : "");

            amazonSes.prepareAndSend(
                    template.getSubject(),
                    body,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    email,
                    List.of(),
                    templateCode,
                    0L
            );

            log.info("Email sent successfully | templateCode={} | email={}", templateCode, email);

        } catch (Exception e) {
            log.error("Failed to send email | templateCode={} | email={} | error={}",
                    templateCode, email, e.getMessage(), e);
            throw e;
        }
    }

    public void sendCommunityOptInEmail(
            String templateCode,
            CommunityOptIn optIn,
            String email
    ) throws Exception {

        try {

            log.info("Sending Community Opt-In email | templateCode={} | email={}",
                    templateCode, optIn.getContactEmail());

            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String body = template.getBodyHtml();

            if (body == null || body.isBlank()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
                return;
            }

            body = body.replace("{{applicant.name}}", safe(optIn.getApplicantName()));
            body = body.replace("{{contact.email}}", safe(optIn.getContactEmail()));
            body = body.replace("{{linkedin.url}}", safe(optIn.getLinkedinUrl()));
            body = body.replace("{{job.title}}", safe(optIn.getJobTitle()));
            body = body.replace("{{job.location}}", safe(optIn.getPreferredLocation()));
            body = body.replace("{{additional.info}}", safe(optIn.getAdditionalInformation()));
            body = body.replace("{{license.url}}", "(ONLY ACCESSED BY SHARKDOM TEAM)");

            amazonSes.prepareAndSend(
                    template.getSubject(),
                    body,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    email,
                    List.of(),
                    templateCode,
                    0L
            );

            log.info("Community Opt-In email sent successfully | email={}",
                    optIn.getContactEmail());

        } catch (Exception e) {
            log.error("Failed to send Community Opt-In email | error={}",
                    e.getMessage(), e);
            throw e;
        }
    }

    public void sendCommunityOptInEmailToUSER(
            String templateCode,
            CommunityOptIn optIn,
            String email
    ) throws Exception {

        try {

            log.info("Sending Community Opt-In email | templateCode={} | email={}",
                    templateCode, optIn.getContactEmail());

            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String body = template.getBodyHtml();

            if (body == null || body.isBlank()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
                return;
            }

            body = body.replace("{{applicant.name}}", safe(optIn.getApplicantName()));
            body = body.replace("{{contact.email}}", safe(optIn.getContactEmail()));
            body = body.replace("{{linkedin.url}}", safe(optIn.getLinkedinUrl()));
            body = body.replace("{{job.title}}", safe(optIn.getJobTitle()));
            body = body.replace("{{job.location}}", safe(optIn.getPreferredLocation()));
            body = body.replace("{{additional.info}}", safe(optIn.getAdditionalInformation()));
            body = body.replace("{{license.url}}", "(ONLY ACCESSED BY SHARKDOM TEAM)");

            amazonSes.prepareAndSend(
                    template.getSubject(),
                    body,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    email,
                    List.of(),
                    templateCode,
                    0L
            );

            log.info("Community Opt-In email sent successfully | email={}",
                    optIn.getContactEmail());

        } catch (Exception e) {
            log.error("Failed to send Community Opt-In email | error={}",
                    e.getMessage(), e);
            throw e;
        }
    }

    public void sendCommunityOptInEmailToADMIN(
            String templateCode,
            CommunityOptIn optIn,
            String email
    ) throws Exception {

        try {

            log.info("Sending Community Opt-In email | templateCode={} | email={}",
                    templateCode, optIn.getContactEmail());

            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String body = template.getBodyHtml();

            if (body == null || body.isBlank()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
                return;
            }

            body = body.replace("{{applicant.name}}", safe(optIn.getApplicantName()));
            body = body.replace("{{contact.email}}", safe(optIn.getContactEmail()));
            body = body.replace("{{linkedin.url}}", safe(optIn.getLinkedinUrl()));
            body = body.replace("{{job.title}}", safe(optIn.getJobTitle()));
            body = body.replace("{{job.location}}", safe(optIn.getPreferredLocation()));
            body = body.replace("{{additional.info}}", safe(optIn.getAdditionalInformation()));
            body = body.replace("{{license.url}}", safe(optIn.getLicensePdfUrl()));

            amazonSes.prepareAndSend(
                    template.getSubject(),
                    body,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    email,
                    List.of(),
                    templateCode,
                    0L
            );

            log.info("Community Opt-In email sent successfully | email={}",
                    optIn.getContactEmail());

        } catch (Exception e) {
            log.error("Failed to send Community Opt-In email | error={}",
                    e.getMessage(), e);
            throw e;
        }
    }

    public void sendCompanyPartnerCreatedEmail(
            String templateCode,
            CompanyPartnerApplication cp,
            String email
    ) throws Exception {

        try {
            log.info("Sending CompanyPartnerApplication email to internal team: {}", email);

            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String body = template.getBodyHtml();

            if (body == null || body.isBlank()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
                return;
            }

            // Replace placeholders
            body = body.replace("{{company.name}}", safe(cp.getCompanyName()));
            body = body.replace("{{company.website}}", safe(cp.getCompanyWebsite()));
            body = body.replace("{{contact.name}}", safe(cp.getPrimaryContactName()));
            body = body.replace("{{contact.email}}", safe(cp.getContactEmail()));
            body = body.replace("{{company.size}}", cp.getCompanySize() != null ? cp.getCompanySize().name() : "");
            body = body.replace("{{partner.type}}", cp.getPartnerType() != null ? cp.getPartnerType().name() : "");
            body = body.replace("{{maturity}}", cp.getMaturity() != null ? cp.getMaturity().name() : "");
            body = body.replace("{{relationship}}",
                    Boolean.TRUE.equals(cp.getHasExistingRelationship()) ? "Yes" : "No");
            body = body.replace("{{icp.fit}}", safe(cp.getIcpFitExplanation()));

            amazonSes.prepareAndSend(
                    template.getSubject(),
                    body,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    email,
                    List.of(),
                    templateCode,
                    0L
            );

            log.info("CompanyPartnerApplication email sent successfully to internal team: {}", email);

        } catch (Exception e) {
            log.error("Failed to send CompanyPartnerApplication email | error={}", e.getMessage(), e);
            throw e;
        }
    }

    public void sendPartnerCreatedEmail(
            String templateCode,
            PartnerApplication cp,
            String email
    ) throws Exception {

        try {
            log.info("Sending CompanyPartnerApplication email to internal team: {}", email);

            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String body = template.getBodyHtml();

            if (body == null || body.isBlank()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
                return;
            }

            // Replace placeholders
            body = body.replace("{{company.name}}", safe(cp.getCompanyName()));
//            body = body.replace("{{company.website}}", safe(cp.getCompanyWebsite()));
//            body = body.replace("{{contact.name}}", safe(cp.getPrimaryContactName()));
//            body = body.replace("{{contact.email}}", safe(cp.getContactEmail()));
//            body = body.replace("{{company.size}}", cp.getCompanySize() != null ? cp.getCompanySize().name() : "");
//            body = body.replace("{{partner.type}}", cp.getPartnerType() != null ? cp.getPartnerType().name() : "");
//            body = body.replace("{{maturity}}", cp.getMaturity() != null ? cp.getMaturity().name() : "");
//            body = body.replace("{{relationship}}",
//                    Boolean.TRUE.equals(cp.getHasExistingRelationship()) ? "Yes" : "No");
//            body = body.replace("{{icp.fit}}", safe(cp.getIcpFitExplanation()));

            amazonSes.prepareAndSend(
                    template.getSubject(),
                    body,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    email,
                    List.of(),
                    templateCode,
                    0L
            );

            log.info("CompanyPartnerApplication email sent successfully to internal team: {}", email);

        } catch (Exception e) {
            log.error("Failed to send CompanyPartnerApplication email | error={}", e.getMessage(), e);
            throw e;
        }
    }

    public void sendConsultantPartnerCreatedEmail(
            String templateCode,
            ConsultantPartnerApplication cp,
            String email
    ) throws Exception {

        try {
            log.info("Sending ConsultantPartnerApplication email to internal team: {}", email);

            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String body = template.getBodyHtml();

            if (body == null || body.isBlank()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
                return;
            }

            // Replace placeholders
            body = body.replace("{{full.name}}", safe(cp.getFullName()));
            body = body.replace("{{email}}", safe(cp.getEmail()));
            body = body.replace("{{linkedin}}", safe(cp.getLinkedinProfileUrl()));
            body = body.replace("{{country}}", cp.getCountry() != null ? cp.getCountry().name() : "");
            body = body.replace("{{role}}", cp.getRoleDescription() != null ? cp.getRoleDescription().name() : "");
            body = body.replace("{{advisory.count}}", cp.getAdvisoryCount() != null ? cp.getAdvisoryCount().name() : "");
            body = body.replace("{{arr.range}}", cp.getArrRange() != null ? cp.getArrRange().name() : "");
            body = body.replace("{{client.arr.range}}", cp.getTypicalClientArrRange() != null ? cp.getTypicalClientArrRange().name() : "");
            body = body.replace("{{program.status}}", cp.getPartnerProgramStatus() != null ? cp.getPartnerProgramStatus().name() : "");
            body = body.replace("{{lead.source}}", cp.getLeadSource() != null ? cp.getLeadSource().name() : "");

            body = body.replace("{{use.bot}}",
                    Boolean.TRUE.equals(cp.getUseDweepBot()) ? "Yes" : "No");

            body = body.replace("{{commission.accepted}}",
                    Boolean.TRUE.equals(cp.getAcceptCommissionTerms()) ? "Yes" : "No");

            body = body.replace("{{terms.accepted}}",
                    Boolean.TRUE.equals(cp.getAgreeToTerms()) ? "Yes" : "No");

            amazonSes.prepareAndSend(
                    template.getSubject(),
                    body,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    email,
                    List.of(),
                    templateCode,
                    0L
            );

            log.info("ConsultantPartnerApplication email sent successfully to internal team: {}", email);

        } catch (Exception e) {
            log.error("Failed to send ConsultantPartnerApplication email | error={}", e.getMessage(), e);
            throw e;
        }
    }

    public void sendPartnerInterestNotificationEmail(
            String templateCode,
            String receiverEmail,
            String receiverName
    ) throws Exception {

        try {
            log.info("Sending Partner Interest email to: {}", receiverEmail);

            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);
            String body = template.getBodyHtml();

            if (body == null || body.isBlank()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
                return;
            }

            // Replace placeholders
            body = body.replace("{{user.name}}", safe(receiverName));

            amazonSes.prepareAndSend(
                    template.getSubject(),
                    body,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    receiverEmail,
                    List.of(),
                    templateCode,
                    0L
            );

            log.info("Partner Interest email sent successfully to: {}", receiverEmail);

        } catch (Exception e) {
            log.error("Failed to send Partner Interest email | error={}", e.getMessage(), e);
            throw e;
        }
    }

    public void sendEmailUsingTemplate(
            String templateCode,
            String receiverEmail,
            String receiverName,
            Map<String, String> placeholders
    ) throws Exception {

        try {

            log.info("Sending email | templateCode={} | receiverEmail={}", templateCode, receiverEmail);

            EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateCode);

            if (template == null) {
                log.warn("Email template not found | templateCode={}", templateCode);
                return;
            }

            String body = template.getBodyHtml();

            if (body == null || body.isBlank()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
                return;
            }

            // Default placeholder
            body = body.replace("{{user.name}}", safe(receiverName));

            // Dynamic placeholders
            if (placeholders != null && !placeholders.isEmpty()) {
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {

                    String key = "{{" + entry.getKey() + "}}";
                    String value = safe(entry.getValue());

                    body = body.replace(key, value);
                }
            }

            amazonSes.prepareAndSend(
                    template.getSubject(),
                    body,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    receiverEmail,
                    List.of(),
                    templateCode,
                    0L
            );

            log.info("Email sent successfully | templateCode={} | receiverEmail={}", templateCode, receiverEmail);

        } catch (Exception e) {

            log.error("Failed to send email | templateCode={} | error={}", templateCode, e.getMessage(), e);
            throw e;
        }
    }

    public void sendAskForPaymentEmailToCompany(
            String templateCode,
            PartnerApplicationDTO partner,
            String companyEmail,
            String reason,
            String notes
    ) throws Exception {

        try {
            log.info("Sending Ask For Payment email to company: {}", companyEmail);

            EmailTemplate template =
                    emailFromTemplateService.getTemplateByCode(templateCode);

            if (template == null) {
                log.warn("Template not found | templateCode={}", templateCode);
                return;
            }

            String body = template.getBodyHtml();

            if (body == null || body.isBlank()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
                return;
            }

            body = body.replace("{{full.name}}", safe(partner.getFullName()));
            body = body.replace("{{email}}", safe(partner.getEmail()));
            body = body.replace("{{company.name}}", safe(partner.getCompanyName()));
            body = body.replace("{{geography}}", safe(partner.getGeography()));
            body = body.replace("{{refer.code}}", safe(partner.getReferCode()));

            body = body.replace(
                    "{{partnership.tier}}",
                    partner.getPartnershipTier() != null
                            ? partner.getPartnershipTier().name()
                            : ""
            );

            body = body.replace("{{payment.reason}}", safe(reason));
            body = body.replace("{{payment.notes}}", safe(notes));

            amazonSes.prepareAndSend(
                    template.getSubject(),
                    body,
                    List.of(),
                    template.getSender() != null ? template.getSender() : defaultSender,
                    companyEmail,
                    List.of(),
                    templateCode,
                    0L
            );

            log.info("Ask For Payment email sent successfully to company: {}", companyEmail);

        } catch (Exception e) {
            log.error("Failed to send Ask For Payment email | error={}", e.getMessage(), e);
            throw e;
        }
    }

    public void sendDemoBookDetailsEmail(
            String templateCode,
            DemoBook demoBook,
            String receiverEmail
    ) throws Exception {

        try {

            log.info("Sending Demo Book Details email to: {}", receiverEmail);

            EmailTemplate template =
                    emailFromTemplateService.getTemplateByCode(templateCode);

            if (template == null) {
                log.warn("Template not found | templateCode={}", templateCode);
                return;
            }

            String body = template.getBodyHtml();

            if (body == null || body.isBlank()) {
                log.warn("Email body is empty | templateCode={}", templateCode);
                return;
            }

            body = body.replace("{{first.name}}", safe(demoBook.getFirstName()));
            body = body.replace("{{last.name}}", safe(demoBook.getLastName()));
            body = body.replace("{{business.email}}", safe(demoBook.getBusinessEmail()));
            body = body.replace("{{startup.name}}", safe(demoBook.getStartupName()));
            body = body.replace("{{purpose}}", safe(demoBook.getPurpose()));
            body = body.replace("{{phone.number}}", safe(demoBook.getPhoneNumber()));

            body = body.replace(
                    "{{demo.type}}",
                    demoBook.getDemoType() != null
                            ? demoBook.getDemoType().name()
                            : ""
            );

            amazonSes.prepareAndSend(
                    template.getSubject(),
                    body,
                    List.of(),
                    template.getSender() != null
                            ? template.getSender()
                            : defaultSender,
                    receiverEmail,
                    List.of(),
                    templateCode,
                    0L
            );

            log.info("Demo Book Details email sent successfully to: {}", receiverEmail);

        } catch (Exception e) {

            log.error(
                    "Failed to send Demo Book Details email | error={}",
                    e.getMessage(),
                    e
            );

            throw e;
        }
    }
}
