package com.sharkdom.service.organizationcollaboration;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.sharkdom.config.AppProperties;
import com.sharkdom.config.WebSocketHandler;
import com.sharkdom.constants.*;
import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.constants.partnerDeals.DealStage;
import com.sharkdom.constants.partnerDeals.DealStatus;
import com.sharkdom.dto.OrganizationPartnerResponse;
import com.sharkdom.dto.OrganizationUserMappingResponseDTO;
import com.sharkdom.dto.SaveAssignmentDto;
import com.sharkdom.emailOutreach.entity.EmailAccount;
import com.sharkdom.emailOutreach.repository.EmailAccountRepository;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.entity.mou.MouHistory;
import com.sharkdom.entity.mypartner.MyPartnerAssignment;
import com.sharkdom.entity.notification.Notification;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organization.OrganizationUserMapping;
import com.sharkdom.entity.organizationcollaboration.*;
import com.sharkdom.entity.organizationcollaboration.dto.MessageResponse;
import com.sharkdom.entity.organizationcollaboration.dto.PartnerSpaceRequest;
import com.sharkdom.entity.partenearDeals.Deal;
import com.sharkdom.entity.referral.ReferralEntity;
import com.sharkdom.entity.user.User;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.mapper.OrganizationMessagesMapper;
import com.sharkdom.mapper.ProposalEditHistoryMapper;
import com.sharkdom.model.PersonaStatus;
import com.sharkdom.model.email.TemplateOrganizationEmailReqModel;
import com.sharkdom.model.mou.MouSignRequest;
import com.sharkdom.model.organizatiocollaboration.*;
import com.sharkdom.model.organization.OrganizationUserMappingResponse;
import com.sharkdom.mypartner.entity.MyPartnerSendCredential;
import com.sharkdom.mypartner.repository.SendMyPartnerCredentialRepository;
import com.sharkdom.repository.ai.PersonaStatusRepository;
import com.sharkdom.repository.credits.CreditsRepository;
import com.sharkdom.repository.meetings.MeetingDetailsRepository;
import com.sharkdom.repository.mou.MouHistoryRepository;
import com.sharkdom.repository.mypartner.MyPartnerAssignmentRepository;
import com.sharkdom.repository.notification.NotificationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.organizationcollaboration.*;
import com.sharkdom.repository.partnerDeals.DealRepository;
import com.sharkdom.repository.referral.LeadsRepository;
import com.sharkdom.repository.referral.ReferralRepository;
import com.sharkdom.repository.user.SlackIntegrationRepository;
import com.sharkdom.service.ai.PersonaService;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.mou.MouPdfGenerator;
import com.sharkdom.service.notification.NotificationService;
import com.sharkdom.service.user.SlackService;
import com.sharkdom.util.AzureStorageService;
import com.sharkdom.util.Util;
import com.sharkdom.util.aws.service.AmazonS3Service;
import com.sharkdom.zoho.entity.ZohoSignedDocumentEntity;
import com.sharkdom.zoho.repository.ZohoSignedDocumentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sharkdom.constants.Constants.*;

@Service
@Slf4j
public class OrganizationCollaborationService {

    private ObjectMapper objectMapper;
    @Value("${env}")
    private String env;
    @Value("${message.encryption-key}")
    private String messageEncryptionKey;
    private final OfflineContractRepository offlineContractRepository;
    private OrganizationCollaborationNotificationService notificationService;
    private final AmazonS3Service amazonS3Service;
    private final OrganizationCollaborationCategoryRepository organizationCollaborationCategoryRepository;
    private final OrganizationCollaborationRepository organizationCollaborationRepository;
    private final CreditsRepository creditsRepository;
    private final ScheduledExecutorService scheduler;
    private final MeetingDetailsRepository meetingDetailsRepository;
    private final AppProperties appProperties;
    private final EmailService emailService;
    private final PartnershipMouVersionRepository partnershipMouVersionRepository;
    private final MouPdfGenerator mouPdfGenerator;
    private final SlackIntegrationRepository integrationRepository;
    private final OrganizationMessagesRepository organizationMessagesRepository;
    private final OrganizationRepository organizationRepository;
    private final ReceiverBenefitRepository receiverBenefitRepository;
    private final SenderBenefitRepository senderBenefitRepository;
    private final TimelineRepository timelineRepository;
    private final WebSocketHandler webSocketHandler;
    private final EnvelopeRepository envelopeRepository;
    private final MouHistoryRepository mouHistoryRepository;
    private final NotificationRepository notificationRepository;
    private final AzureStorageService azureStorageService;
    private final NotificationService notificationServiceNote;
    private final PartnerSpaceRepository partnerSpaceRepository;
    private final OrganizationUserMappingRepository organizationUserMappingRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ProposalEditHistoryRepository proposalEditHistoryRepository;
    private final PersonaStatusRepository personaStatusRepository;
    private final SlackService slackService;
    private final ZohoSignedDocumentRepository zohoSignedDocumentRepository;
    private final DealRepository dealRepository;
    private final SendMyPartnerCredentialRepository sendMyPartnerCredentialRepository;
    private final ReferralRepository referralRepository;
    private final LeadsRepository leadRepository;
    @Autowired
    private EmailAccountRepository emailAccountRepository;
    @Autowired
    private MyPartnerAssignmentRepository myPartnerAssignmentRepository;

    public OrganizationCollaborationService(ObjectMapper objectMapper,
                                            OfflineContractRepository offlineContractRepository,
                                            OrganizationCollaborationNotificationService notificationService, AmazonS3Service amazonS3Service,
                                            OrganizationCollaborationCategoryRepository organizationCollaborationCategoryRepository,
                                            OrganizationCollaborationRepository organizationCollaborationRepository,
                                            CreditsRepository creditsRepository, MeetingDetailsRepository meetingDetailsRepository,
                                            AppProperties appProperties, EmailService emailService,
                                            PartnershipMouVersionRepository partnershipMouVersionRepository, MouPdfGenerator mouPdfGenerator,
                                            SlackIntegrationRepository integrationRepository, OrganizationMessagesRepository organizationMessagesRepository,
                                            OrganizationRepository organizationRepository, ReceiverBenefitRepository receiverBenefitRepository,
                                            SenderBenefitRepository senderBenefitRepository, TimelineRepository timelineRepository,
                                            WebSocketHandler webSocketHandler, EnvelopeRepository envelopeRepository,
                                            MouHistoryRepository mouHistoryRepository, NotificationRepository notificationRepository,
                                            AzureStorageService azureStorageService, NotificationService notificationServiceNote,
                                            PartnerSpaceRepository partnerSpaceRepository, OrganizationUserMappingRepository organizationUserMappingRepository, ProposalEditHistoryRepository proposalEditHistoryRepository, PersonaStatusRepository personaStatusRepository, SlackService slackService, ZohoSignedDocumentRepository zohoSignedDocumentRepository, DealRepository dealRepository, SendMyPartnerCredentialRepository sendMyPartnerCredentialRepository, ReferralRepository referralRepository, ReferralRepository referralRepository1, LeadsRepository leadRepository) {
        this.objectMapper = objectMapper;
        this.offlineContractRepository = offlineContractRepository;
        this.notificationService = notificationService;
        this.amazonS3Service = amazonS3Service;
        this.organizationCollaborationCategoryRepository = organizationCollaborationCategoryRepository;
        this.organizationCollaborationRepository = organizationCollaborationRepository;
        this.creditsRepository = creditsRepository;
        this.integrationRepository = integrationRepository;
        this.webSocketHandler = webSocketHandler;
        this.envelopeRepository = envelopeRepository;
        this.mouHistoryRepository = mouHistoryRepository;
        this.notificationRepository = notificationRepository;
        this.azureStorageService = azureStorageService;
        this.notificationServiceNote = notificationServiceNote;
        this.partnerSpaceRepository = partnerSpaceRepository;
        this.organizationUserMappingRepository = organizationUserMappingRepository;
        this.proposalEditHistoryRepository = proposalEditHistoryRepository;
        this.personaStatusRepository = personaStatusRepository;
        this.slackService = slackService;
        this.zohoSignedDocumentRepository = zohoSignedDocumentRepository;
        this.dealRepository = dealRepository;
        this.sendMyPartnerCredentialRepository = sendMyPartnerCredentialRepository;
        this.referralRepository = referralRepository1;
        this.leadRepository = leadRepository;
        this.scheduler = Executors.newScheduledThreadPool(10);
        this.meetingDetailsRepository = meetingDetailsRepository;
        this.appProperties = appProperties;
        this.emailService = emailService;
        this.partnershipMouVersionRepository = partnershipMouVersionRepository;
        this.mouPdfGenerator = mouPdfGenerator;
        this.organizationMessagesRepository = organizationMessagesRepository;
        this.organizationRepository = organizationRepository;
        this.receiverBenefitRepository = receiverBenefitRepository;
        this.senderBenefitRepository = senderBenefitRepository;
        this.timelineRepository = timelineRepository;
    }

    @Transactional
    public OrganizationCollaboration createOrUpdateCollaboration(OrganizationCollaboration collaboration)
            throws Exception {

        log.info("Initiating createOrUpdateCollaboration for senderOrgId={} and receiverOrgId={}",
                collaboration.getSenderOrganizationId(), collaboration.getReceiverOrganizationId());

        Optional<OrganizationCollaboration> existingCollaboration = organizationCollaborationRepository
                .findBySenderOrganizationIdAndReceiverOrganizationIdOrderById(collaboration.getSenderOrganizationId(),
                        collaboration.getReceiverOrganizationId());

        OrganizationCollaboration organizationCollaboration;

        if (existingCollaboration.isPresent()) {
            log.info("Existing collaboration found with ID={}", existingCollaboration.get().getId());

            collaboration.setId(existingCollaboration.get().getId());
            organizationCollaboration = update(collaboration);

            log.debug("Updated collaboration, setting organization names.");
            organizationCollaboration.setReceiverOrganizationName(
                    organizationRepository.findNameById(organizationCollaboration.getReceiverOrganizationId()));
            organizationCollaboration.setSenderOrganizationName(
                    organizationRepository.findNameById(organizationCollaboration.getSenderOrganizationId()));
        } else {
            log.info("No existing collaboration found. Proceeding to create a new one.");

            var credits = creditsRepository.findByOrganizationId(collaboration.getSenderOrganizationId());

            if (credits == null || credits.getCollaborationsLeft() < 1) {
                log.error("Insufficient credits or credits not found for orgId={}", collaboration.getSenderOrganizationId());
                throw new SharkdomException(ErrorMessages.SH109);
            }

            log.info("Credits check passed. Creating collaboration.");
            organizationCollaboration = create(collaboration);

            organizationCollaboration.setReceiverOrganizationName(
                    organizationRepository.findNameById(organizationCollaboration.getReceiverOrganizationId()));
            organizationCollaboration.setSenderOrganizationName(
                    organizationRepository.findNameById(organizationCollaboration.getSenderOrganizationId()));

            String templateCode = appProperties.getEmailTemplateCodeForEvent(PARTNERSHIP_INITIATED_TEMPLATE);

            log.debug("Sending email to sender organization.");
            if (organizationCollaboration.getPartnershipMouVersions() == null ||
                    organizationCollaboration.getPartnershipMouVersions().isEmpty()) {
                log.error("Partnership MOU versions are missing for collaborationId={}", organizationCollaboration.getId());

            } else {
                emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                                .templateCode(templateCode)
                                .senderOrganizationName(organizationCollaboration.getReceiverOrganizationName())
                                .organizationName(
                                        organizationRepository.findNameById(organizationCollaboration.getSenderOrganizationId()))
                                .partnerOrganizationName(organizationCollaboration.getReceiverOrganizationName())
                                .partnershipInitiationTime(LocalDate.now())
                                .organizationIds(List.of(organizationCollaboration.getSenderOrganizationId())).build(), null,
                        organizationCollaboration.getId(),
                        organizationCollaboration.getPartnershipMouVersions().get(0).getId());

                log.debug("Sending email to receiver organization.");
                emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                                .templateCode(templateCode)
                                .senderOrganizationName(organizationCollaboration.getSenderOrganizationName())
                                .organizationName(
                                        organizationRepository.findNameById(organizationCollaboration.getReceiverOrganizationId()))
                                .partnershipInitiationTime(LocalDate.now())
                                .partnerOrganizationName(organizationCollaboration.getSenderOrganizationName())
                                .organizationIds(List.of(organizationCollaboration.getReceiverOrganizationId())).build(), null,
                        organizationCollaboration.getId(),
                        organizationCollaboration.getPartnershipMouVersions().get(0).getId());
            }
        }
        log.info("Completed createOrUpdateCollaboration for collaborationId={}", organizationCollaboration.getId());
        return organizationCollaboration;
    }


    @Transactional
    public OrganizationCollaboration create(OrganizationCollaboration organizationCollaboration) {
        var credits = creditsRepository.findByOrganizationId(organizationCollaboration.getSenderOrganizationId());
        credits.setCollaborationsLeft(credits.getCollaborationsLeft() - 1);
        organizationCollaboration = organizationCollaborationRepository.save(organizationCollaboration);
        notificationService.sendNewCollabRequestNotification(organizationCollaboration);
        creditsRepository.save(credits);

        var slackIntegration = integrationRepository.findByUserId(organizationCollaboration.getSenderUserId());
        log.error("slackIntegration {}", slackIntegration);
        if (slackIntegration.isPresent()) {
            slackService.sendProposalRequest(organizationCollaboration.getId(), slackIntegration.get().getUserId(), slackIntegration.get().getChannelId());
        }
        return organizationCollaboration;
    }

    @Transactional
    public OrganizationCollaboration update(OrganizationCollaboration updated) throws Exception {
        OrganizationCollaboration existing = findById(updated.getId());

        // Update basic properties
        existing.setSenderOrganizationId(updated.getSenderOrganizationId());
        existing.setReceiverOrganizationId(updated.getReceiverOrganizationId());
        existing.setSenderUserId(updated.getSenderUserId());
        existing.setAcceptorUserId(updated.getAcceptorUserId());
        existing.setStatus(updated.getStatus());
        existing.setSenderUrlsJson(updated.getSenderUrlsJson());
        existing.setReceiverUrlsJson(updated.getReceiverUrlsJson());
        existing.setChatAccessAllowed(updated.isChatAccessAllowed());
        existing.setContactPersonUserId(updated.getContactPersonUserId());

        // Update partnershipMouVersions
        if (updated.getPartnershipMouVersions() != null && !updated.getPartnershipMouVersions().isEmpty()) {
            if (existing.getPartnershipMouVersions() == null) {
                existing.setPartnershipMouVersions(new ArrayList<>());
            }

            // Add new partnershipMouVersions to the existing list
            List<Long> existingIds = existing.getPartnershipMouVersions().stream()
                    .map(PartnershipMouVersion::getId)
                    .toList();

            for (PartnershipMouVersion mouVersion : updated.getPartnershipMouVersions()) {
                if (mouVersion.getId() == null || !existingIds.contains(mouVersion.getId())) {
                    existing.getPartnershipMouVersions().add(mouVersion);
                }
            }
        }

        return organizationCollaborationRepository.save(existing);
    }

    @Transactional
    public HttpStatus acceptCollabRequest(long senderOrganizationId, long receiverOrganizationId,
                                          String acceptorUserId) {
        Optional<OrganizationCollaboration> organizationCollaborationOptional = organizationCollaborationRepository
                .findBySenderOrganizationIdAndReceiverOrganizationIdOrderById(senderOrganizationId,
                        receiverOrganizationId);
        if (organizationCollaborationOptional.isPresent()) {
            OrganizationCollaboration organizationCollaboration = organizationCollaborationOptional.get();
            organizationCollaboration.setStatus("ACTIVE");
            organizationCollaboration.setAcceptorUserId(acceptorUserId);
            organizationCollaborationRepository.save(organizationCollaboration);
            notificationService.sendCollabRequestAcceptedNotification(organizationCollaboration);
            mouPdfGenerator.generateMou(organizationCollaboration);
            TimelineEntity timelineEntity = TimelineEntity.builder()
                    .organizationCollaborationId(organizationCollaboration.getId())
                    .action(String.format("%s initiated the proposal",
                            organizationRepository.findNameById(organizationCollaboration.getSenderOrganizationId())))
                    .build();
            timelineRepository.save(timelineEntity);
            scheduleMeetingWindow(organizationCollaboration.getId(),
                    organizationCollaboration.getSenderOrganizationId());

            TimelineEntity signingProcessStarted = TimelineEntity.builder()
                    .organizationCollaborationId(organizationCollaboration.getId())
                    .action("signing process started").build();
            timelineRepository.save(signingProcessStarted);
            var credits = creditsRepository.findByOrganizationId(organizationCollaboration.getSenderOrganizationId());
            credits.setCollaborationsLeft(credits.getCollaborationsLeft() + 1);
            creditsRepository.save(credits);
            return HttpStatus.OK;

        }
        return HttpStatus.NOT_FOUND;

    }

    private OrganizationCollaboration findById(long id) {
        return organizationCollaborationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH33, id));
    }

    public Page<OrganizationCollaboration> findBySenderOrganizationId(int page, int size) {
        var senderOrganizationId = Util.getOrgIdFromToken();
        Page<OrganizationCollaboration> collaborations = organizationCollaborationRepository
                .getAllBySenderOrganizationId(senderOrganizationId, PageRequest.of(page, size));

        // Map through the content of the page and set the category for each
        // collaboration
        collaborations.getContent().forEach(organizationCollaboration -> {
            organizationCollaboration.setCollaborationCategory(
                    organizationCollaborationCategoryRepository
                            .findCategoryByOrganizationCollaborationIdAndOrganizationId(
                                    organizationCollaboration.getId(), senderOrganizationId));
        });

        return collaborations;
    }

    public Page<OrganizationCollaboration> findByReceiverOrganizationId(int page,
                                                                        int size) {
        var receiverOrganizationId = Util.getOrgIdFromToken();
        var collaborations = organizationCollaborationRepository.getAllByReceiverOrganizationId(receiverOrganizationId,
                PageRequest.of(page, size));
        collaborations.getContent().forEach(organizationCollaboration -> {
            organizationCollaboration.setCollaborationCategory(
                    organizationCollaborationCategoryRepository
                            .findCategoryByOrganizationCollaborationIdAndOrganizationId(
                                    organizationCollaboration.getId(), receiverOrganizationId));
        });
        return collaborations;
    }

    @Transactional
    public OrganizationCollaboration patchByOrganizationCollaborationId(long id, JsonPatch patch) throws Exception {
        OrganizationCollaboration organizationCollaboration = findById(id);
        OrganizationCollaboration organizationCollaborationPatched = applyPatchToOrganizationCollaboration(patch,
                organizationCollaboration);
        return organizationCollaborationRepository.save(organizationCollaborationPatched);
    }

    @Transactional
    public OrganizationCollaboration getOrganizationCollaborationById(Long id) {
        boolean isActive=false;
        boolean isOctCome=false;
        Optional<OrganizationCollaboration> organizationCollaborationOptional = organizationCollaborationRepository
                .findById(id);
        if (organizationCollaborationOptional.isPresent()) {
            var res = organizationCollaborationOptional.get();
            var orgIdFromToken = Util.getOrgIdFromToken();
            if (res.getReceiverOrganizationId() != orgIdFromToken && res.getSenderOrganizationId() != orgIdFromToken) {
                throw new ServiceException(ErrorMessages.SH13);
            }
            res.getPartnershipMouVersions().stream().findFirst().ifPresent(mou -> {
                mou.setReceiverBenefits(mou.getReceiverBenefits().stream().filter(receiverBenefit -> Objects.isNull(receiverBenefit.getStatus()) || receiverBenefit.getStatus().equals(BenefitsStatus.ACTIVE)).toList());
                mou.setSenderBenefits(mou.getSenderBenefits().stream().filter(senderBenefit -> Objects.isNull(senderBenefit.getStatus()) || senderBenefit.getStatus().equals(BenefitsStatus.ACTIVE)).toList());
            });
            String senderOrganizationName = organizationRepository.findNameById(res.getSenderOrganizationId());
            String receiverOrganizationName = organizationRepository.findNameById(res.getReceiverOrganizationId());
            String senderLogo = organizationRepository.findLogoUrlById(res.getSenderOrganizationId());
            String receiverLogo = organizationRepository.findLogoUrlById(res.getReceiverOrganizationId());
            var persona = personaStatusRepository.getByOrganizationId(res.getSenderOrganizationId());
            res.setSenderPersonaCreated(persona != null && persona.getPersonaStatus().equals(PersonaStatus.COMPLETED));
            res.setSenderLogo(senderLogo);
            res.setReceiverLogo(receiverLogo);
            res.setSenderOrganizationName(senderOrganizationName);
            res.setReceiverOrganizationName(receiverOrganizationName);
            res.setMeetingDetails(meetingDetailsRepository.findByOrganizationCollaborationId(id));

            List<Deal> listDeals = dealRepository.findByDealerOrgIdAndVendorOrgId(res.getSenderOrganizationId(), res.getReceiverOrganizationId());
            isActive = listDeals.stream()
                    .anyMatch(d -> DealStatus.PENDING.equals(d.getDealStatus()));

            List<PartnerSpaceRoom> partnerCreated = partnerSpaceRepository.findAllByPartnerCreated(res.getReceiverOrganizationId());
            if (partnerCreated != null && !partnerCreated.isEmpty()) {
                isActive=true;
            }

            List<MyPartnerSendCredential> myPartnerSendCredentials = sendMyPartnerCredentialRepository.findBySenderIdAndReceiverId(String.valueOf(res.getSenderOrganizationId()), String.valueOf(res.getReceiverOrganizationId()));
            if (myPartnerSendCredentials != null && !myPartnerSendCredentials.isEmpty()) {
                isActive=true;
            }
            res.setActivation(isActive);

            List<Deal> listDeal = dealRepository.findByDealerOrgIdAndVendorOrgId(
                    res.getSenderOrganizationId(),
                    res.getReceiverOrganizationId()
            );
            boolean isApproved = listDeal.stream()
                    .anyMatch(d -> DealStage.APPROVED.equals(d.getDealStage()));
            if (isApproved) {
                isOctCome=true;
            }

            List<ReferralEntity> referrals = referralRepository.findByOrganizationId(res.getSenderOrganizationId());

            for (ReferralEntity referralEntity : referrals) {
                String referralCode = referralEntity.getReferralCode();
                int count = leadRepository.countByReferralCode(referralCode);
                if (count > 0) {
                    isOctCome = true;
                    break;
                }
            }
            Optional<EmailAccount> emailAccount = emailAccountRepository.findByOrganizationId(Util.getOrgIdFromToken());
            if (emailAccount.isPresent())
            {
                res.setMailBoxClaimed(true);
            }
            res.setOutcome(isOctCome);
            return res;
        } else {
            throw new ResourceNotFoundException(ErrorMessages.SH33, id);
        }
    }

    private OrganizationCollaboration applyPatchToOrganizationCollaboration(JsonPatch patch,
                                                                            OrganizationCollaboration targetUser)

            throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(targetUser, JsonNode.class));
        return objectMapper.treeToValue(patched, OrganizationCollaboration.class);
    }

    public OrganizationCollaboration findBySenderOrganizationIdAndReceiverId(long senderOrganizationId,
                                                                             long receiverOrganizationId) {
        return organizationCollaborationRepository
                .findBySenderOrganizationIdAndReceiverOrganizationIdOrderById(senderOrganizationId,
                        receiverOrganizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.SH34, receiverOrganizationId, senderOrganizationId));
    }

    public OrganizationCollaboration findCollabBetweenTwoOrganizations(long senderOrganizationId,
                                                                       long receiverOrganizationId) {
        Optional<OrganizationCollaboration> collab = organizationCollaborationRepository
                .findBySenderOrganizationIdAndReceiverOrganizationIdOrderById(senderOrganizationId,
                        receiverOrganizationId);
        if (!collab.isPresent()) {
            collab = organizationCollaborationRepository.findBySenderOrganizationIdAndReceiverOrganizationIdOrderById(
                    receiverOrganizationId, senderOrganizationId);
        }
        return collab.orElseThrow(() -> new ResourceNotFoundException(
                ErrorMessages.SH34, receiverOrganizationId, senderOrganizationId));
    }

    @Scheduled(cron = "0 0 14 * * *")
    public void triggerAfter2DaysOfPartnershipInitiated() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -2);
        Date twoDaysAgo = calendar.getTime();
        List<Long> pendingOrganizationCollaboration = partnershipMouVersionRepository
                .findPendingRecordsCreatedDaysAgo(twoDaysAgo);
        var organizationCollaborationList = organizationCollaborationRepository
                .findAllById(pendingOrganizationCollaboration);
        String templateCode = appProperties.getEmailTemplateCodeForEvent(PARTNERSHIP_EXPIRING_TEMPLATE);
        List<Long> orgIds = new ArrayList<>();
        organizationCollaborationList.forEach(organizationCollaboration -> {
            organizationCollaboration.setReceiverOrganizationName(
                    organizationRepository.findNameById(organizationCollaboration.getReceiverOrganizationId()));
            organizationCollaboration.setSenderOrganizationName(
                    organizationRepository.findNameById(organizationCollaboration.getSenderOrganizationId()));
            emailService.sendByTemplateAndOrganizationIds(
                    TemplateOrganizationEmailReqModel.builder().partnershipExpirationTime(LocalDate.now().plusDays(5))
                            .organizationIds(List.of(organizationCollaboration.getSenderOrganizationId()))
                            .partnerOrganizationName(organizationCollaboration.getReceiverOrganizationName())
                            .templateCode(templateCode).build(),
                    null, organizationCollaboration.getId(),
                    organizationCollaboration.getPartnershipMouVersions().get(0).getId());
            emailService.sendByTemplateAndOrganizationIds(
                    TemplateOrganizationEmailReqModel.builder().partnershipExpirationTime(LocalDate.now().plusDays(5))
                            .organizationIds(List.of(organizationCollaboration.getReceiverOrganizationId()))
                            .partnerOrganizationName(organizationCollaboration.getSenderOrganizationName())
                            .templateCode(templateCode).build(),
                    null, organizationCollaboration.getId(),
                    organizationCollaboration.getPartnershipMouVersions().get(0).getId());
        });
    }

    @Scheduled(cron = "0 0 13 * * *")
    public void triggerAfter7DaysOfPartnershipInitiated() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -8);
        Date sevenDaysAgo = calendar.getTime();
        List<Long> pendingOrganizationCollaboration = partnershipMouVersionRepository
                .findPendingRecordsCreatedDaysAgo(sevenDaysAgo);
        var organizationCollaborationList = organizationCollaborationRepository
                .findAllById(pendingOrganizationCollaboration);
        String templateCode = appProperties.getEmailTemplateCodeForEvent(PARTNERSHIP_EXPIRED_TEMPLATE);
        List<Long> orgIds = new ArrayList<>();
        organizationCollaborationList.forEach(organizationCollaboration -> {
            organizationCollaboration.setReceiverOrganizationName(
                    organizationRepository.findNameById(organizationCollaboration.getReceiverOrganizationId()));
            organizationCollaboration.setSenderOrganizationName(
                    organizationRepository.findNameById(organizationCollaboration.getSenderOrganizationId()));
            emailService.sendByTemplateAndOrganizationIds(
                    TemplateOrganizationEmailReqModel.builder().partnershipExpirationTime(LocalDate.now())
                            .organizationIds(List.of(organizationCollaboration.getSenderOrganizationId()))
                            .partnerOrganizationName(organizationCollaboration.getReceiverOrganizationName())
                            .templateCode(templateCode).build(),
                    null, organizationCollaboration.getId(),
                    organizationCollaboration.getPartnershipMouVersions().get(0).getId());
            emailService.sendByTemplateAndOrganizationIds(
                    TemplateOrganizationEmailReqModel.builder().partnershipExpirationTime(LocalDate.now())
                            .organizationIds(List.of(organizationCollaboration.getReceiverOrganizationId()))
                            .partnerOrganizationName(organizationCollaboration.getSenderOrganizationName())
                            .templateCode(templateCode).build(),
                    null, organizationCollaboration.getId(),
                    organizationCollaboration.getPartnershipMouVersions().get(0).getId());

        });
    }

    public OrganizationMessagesResponse sendMessage(SendMessageRequest sendMessageRequest) {

        if (sendMessageRequest.chatRoomId() == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        if (sendMessageRequest.senderId() == null) {
            throw new IllegalArgumentException("SenderId cannot be null");
        }

        if (LinkerType.RECEIVER_BENEFITS.name().equalsIgnoreCase(sendMessageRequest.linkerType().name())) {
            Optional<ReceiverBenefit> receiverBenefit = receiverBenefitRepository
                    .findById(sendMessageRequest.linkerId());
            if (receiverBenefit.isPresent()) {
                receiverBenefit.get().setActiveConversation(true);
                receiverBenefitRepository.save(receiverBenefit.get());
            }
        } else if (LinkerType.SENDER_BENEFITS.name().equalsIgnoreCase(sendMessageRequest.linkerType().name())) {
            Optional<SenderBenefit> senderBenefit = senderBenefitRepository
                    .findById(sendMessageRequest.linkerId());
            if (senderBenefit.isPresent()) {
                senderBenefit.get().setActiveConversation(true);
                senderBenefitRepository.save(senderBenefit.get());
            }
        }
        var encryptedMessage = Util.encryptForDatabase(sendMessageRequest.query(), messageEncryptionKey);
        var organizationMessages = OrganizationMessages.builder()
                .channelFlag(getChannelCategory(sendMessageRequest.query()))
                .flag(sendMessageRequest.flag())
                .linkerId(sendMessageRequest.linkerId())
                .linkerType(sendMessageRequest.linkerType())
                .chatRoomId(sendMessageRequest.chatRoomId())
                .query(encryptedMessage)
                .senderId(sendMessageRequest.senderId())
                .receiverId(sendMessageRequest.receiverId())
                .isEncrypted(true)
                .build();


        Optional<PartnerSpaceRoom> partnerSpaceOpt = partnerSpaceRepository.findByChatRoomId(sendMessageRequest.chatRoomId());

        if (partnerSpaceOpt.isEmpty()) {
            List<Integer> joinedPartners = new ArrayList<>();
            if (sendMessageRequest.receiverId() != null) {
                joinedPartners.add(sendMessageRequest.receiverId().intValue());
            }
            var senderName = organizationRepository.findNameById(sendMessageRequest.senderId());
            var receiverName = organizationRepository.findNameById(sendMessageRequest.receiverId());
            PartnerSpaceRoom newSpace = PartnerSpaceRoom.builder()
                    .chatRoomId(sendMessageRequest.chatRoomId())
                    .partnerCreated(sendMessageRequest.senderId())
                    .partnerJoined(joinedPartners)
                    .spaceType(SpaceType.OTHER)
                    .spaceName(String.format("%s X %s ", senderName, receiverName))
                    .build();

            partnerSpaceRepository.save(newSpace);
        }
        var savedMessage = organizationMessagesRepository.save(organizationMessages);
        return OrganizationMessagesMapper.toOrganizationMessagesResponse(savedMessage, sendMessageRequest.query());
    }

    public MessageResponse getMessageResponse(Long chatRoomId, ChannelFlag channelType, Flag flag, int page, int size) {
        // First check if chat room exists by trying to find any messages
        boolean chatRoomExists = organizationMessagesRepository.existsByChatRoomId(chatRoomId);

        if (!chatRoomExists) {
            // Return empty response if chat room doesn't exist
            MessageResponse emptyResponse = new MessageResponse();
            emptyResponse.setMessages(new ArrayList<>());
            emptyResponse.setMessageByChannel(new EnumMap<>(ChannelFlag.class));
            emptyResponse.setTotalMessageCount(0);
            emptyResponse.setCurrentPage(page);
            emptyResponse.setPageSize(size);
            emptyResponse.setTotalPages(0);
            emptyResponse.setHasNext(false);
            emptyResponse.setHasPrevious(false);
            emptyResponse.setSpaceName(null);
            emptyResponse.setTotalMemberCount(0);
            emptyResponse.setActiveOrganizationCount(0);
            emptyResponse.setPartners(new ArrayList<>());
            emptyResponse.setChannels(new ArrayList<>());
            return emptyResponse;
        }

        // Create pageable object for pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by("creationTimestamp").descending());

        // Get paginated messages
        Page<OrganizationMessages> messagesPage;
        if (channelType != null && flag != null) {
            messagesPage = organizationMessagesRepository.findAllByChatRoomIdAndChannelFlagAndFlagOrderByCreationTimestampDesc(chatRoomId, channelType, flag, pageable);
        } else if (channelType != null) {
            messagesPage = organizationMessagesRepository.findAllByChatRoomIdAndChannelFlagOrderByCreationTimestampDesc(chatRoomId, channelType, pageable);
        } else if (flag != null) {
            messagesPage = organizationMessagesRepository.findAllByChatRoomIdAndFlagOrderByCreationTimestampDesc(chatRoomId, flag, pageable);
        } else {
            messagesPage = organizationMessagesRepository.findAllByChatRoomIdOrderByCreationTimestampDesc(chatRoomId, pageable);
        }

        List<OrganizationMessages> pagedMessages = messagesPage.getContent();
        messagesPage.getContent().forEach(msg -> {
            if (msg.getChannelFlag() == null) {
                msg.setChannelFlag(ChannelFlag.GENERAL);
            }
            if (msg.isEncrypted()) {
                msg.setQuery(Util.decryptFromDatabase(msg.getQuery(), messageEncryptionKey));
            }
            if (LinkerType.SENDER_BENEFITS.equals(msg.getLinkerType())) {
                Optional<SenderBenefit> senderBenefit = senderBenefitRepository.findById(msg.getLinkerId());
                if (senderBenefit.isPresent()) {
                    msg.setDescription(senderBenefit.get().getDescription());
                    msg.setBenefit(senderBenefit.get().getBenefit());
                }
            } else if (LinkerType.RECEIVER_BENEFITS.equals(msg.getLinkerType())) {
                Optional<ReceiverBenefit> receiverBenefit = receiverBenefitRepository.findById(msg.getLinkerId());
                if (receiverBenefit.isPresent()) {
                    msg.setDescription(receiverBenefit.get().getDescription());
                    msg.setBenefit(receiverBenefit.get().getBenefit());
                }
            }
        });

        // Count all messages for total count
        long totalMessageCount = organizationMessagesRepository.countByChatRoomId(chatRoomId);
        Map<ChannelFlag, List<OrganizationMessages>> messageByChannel = new EnumMap<>(ChannelFlag.class);
        for (ChannelFlag channelFlag : ChannelFlag.values()) {
            messageByChannel.put(channelFlag, new ArrayList<>());
        }
        messageByChannel.putAll(
                pagedMessages.stream()
                        .collect(Collectors.groupingBy(
                                msg -> msg.getChannelFlag() != null ? msg.getChannelFlag() : ChannelFlag.GENERAL,
                                () -> new EnumMap<>(ChannelFlag.class),
                                Collectors.toList()
                        ))
        );

        Optional<PartnerSpaceRoom> partnerSpaceOpt = partnerSpaceRepository.findByChatRoomId(chatRoomId);
        PartnerSpaceRoom room;
        if (partnerSpaceOpt.isEmpty()) {
            // Get sender ID from all messages if available (not just paged messages)
            List<OrganizationMessages> firstMessage = organizationMessagesRepository
                    .findFirstByChatRoomIdOrderByCreationTimestampDesc(chatRoomId);
            Long senderId = firstMessage.isEmpty() ? null : firstMessage.get(0).getSenderId();

            // Create default partner space with null checks
            List<Integer> joinedPartners = new ArrayList<>();
            if (!messagesPage.getContent().isEmpty() && messagesPage.getContent().get(0).getReceiverId() != null) {
                joinedPartners.add(messagesPage.getContent().get(0).getReceiverId().intValue());
            }

            // Safety check for senderId
            if (senderId == null && !messagesPage.getContent().isEmpty()) {
                // Try to get senderId from paged messages if first message retrieval failed
                senderId = messagesPage.getContent().get(0).getSenderId();
            }

            // Create the room only if we have a valid senderId
            if (senderId != null) {
                room = PartnerSpaceRoom.builder()
                        .chatRoomId(chatRoomId)
                        .partnerCreated(senderId)
                        .partnerJoined(joinedPartners)
                        .spaceType(SpaceType.OTHER)
                        .build();

                room = partnerSpaceRepository.save(room);
            } else {
                // Create an empty room if we can't determine the sender
                room = PartnerSpaceRoom.builder()
                        .chatRoomId(chatRoomId)
                        .spaceType(SpaceType.OTHER)
                        .build();
            }
        } else {
            room = partnerSpaceOpt.get();
        }

        Set<Long> activeOrgIds = messagesPage.getContent().stream()
                .filter(msg -> msg.getChannelFlag() != null && !msg.getChannelFlag().equals(ChannelFlag.GENERAL))
                .flatMap(msg -> Stream.of(msg.getSenderId(), msg.getReceiverId()))
                .filter(Objects::nonNull) // Add null check for sender/receiver IDs
                .collect(Collectors.toSet());
        int activeOrganizationCount = activeOrgIds.size();

        List<Map<String, Object>> partners = new ArrayList<>();

        // Add null checks for partnerCreated
        if (room.getPartnerCreated() != null) {
            Map<String, Object> partnerCreatedMap = new HashMap<>();
            partnerCreatedMap.put("organizationId", room.getPartnerCreated());
            partnerCreatedMap.put("logo_url", organizationRepository.findLogoUrlById(room.getPartnerCreated()));
            String orgName = organizationRepository.findNameById(room.getPartnerCreated());
            partnerCreatedMap.put("organizationName", orgName != null ? orgName : "Unknown organization");
            List<OrganizationUserMappingResponse> members = organizationUserMappingRepository.findAllByOrganizationId(room.getPartnerCreated());
            List<Map<String, String>> addedMembersList = new ArrayList<>();
            for (OrganizationUserMappingResponse member : members) {
                Map<String, String> memberData = new HashMap<>();
                memberData.put("role", member.getOrganizationUserMapping().getRole().name());
                memberData.put("name", member.getUser().getName());
                addedMembersList.add(memberData);
            }
            partnerCreatedMap.put("members", addedMembersList);
            partners.add(partnerCreatedMap);
        }

        if (room.getPartnerJoined() != null) {
            for (Integer joinedOrgId : room.getPartnerJoined()) {
                if (joinedOrgId != null) { // Add null check for joined org ID
                    Map<String, Object> joinedMap = new HashMap<>();
                    joinedMap.put("organizationId", joinedOrgId);
                    joinedMap.put("logo_url", organizationRepository.findLogoUrlById(joinedOrgId.longValue()));
                    String joinedOrgName = organizationRepository.findNameById(joinedOrgId.longValue());
                    joinedMap.put("organizationName", joinedOrgName != null ? joinedOrgName : "Unknown organization");
                    List<OrganizationUserMappingResponse> joinedMembers = organizationUserMappingRepository.findAllByOrganizationId(joinedOrgId.longValue());
                    List<Map<String, String>> membersList = new ArrayList<>();
                    for (OrganizationUserMappingResponse member : joinedMembers) {
                        Map<String, String> memberData = new HashMap<>();
                        memberData.put("role", member.getOrganizationUserMapping().getRole().name());
                        memberData.put("name", member.getUser().getName());
                        membersList.add(memberData);
                    }
                    joinedMap.put("members", membersList);
                    partners.add(joinedMap);
                }
            }
        }

        Map<ChannelFlag, Long> channelCountMap = new EnumMap<>(ChannelFlag.class);
        for (ChannelFlag channelFlag : ChannelFlag.values()) {
            channelCountMap.put(channelFlag, 0L);
        }

        channelCountMap.putAll(messagesPage.getContent().stream()
                .filter(msg -> msg.getChannelFlag() != null) // Add null check for channel flag
                .collect(Collectors.groupingBy(
                        msg -> msg.getChannelFlag() != null ? msg.getChannelFlag() : ChannelFlag.GENERAL,
                        Collectors.counting()
                )));

        List<Map<String, Object>> channelList = channelCountMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> ch = new HashMap<>();
                    ch.put("channel", entry.getKey().name());
                    ch.put("messageCount", entry.getValue());
                    return ch;
                })
                .collect(Collectors.toList());

        int totalMemberCount = room.getTotalMembers() != null ? room.getTotalMembers() : 0;

        MessageResponse response = new MessageResponse();
        response.setMessages(messagesPage.getContent());
        response.setMessageByChannel(messageByChannel);
        response.setTotalMessageCount(totalMessageCount);
        response.setCurrentPage(page);
        response.setPageSize(size);
        response.setTotalPages(messagesPage.getTotalPages());
        response.setHasNext(messagesPage.hasNext());
        response.setHasPrevious(messagesPage.hasPrevious());
        response.setSpaceName(room.getSpaceName());
        response.setTotalMemberCount(totalMemberCount);
        response.setActiveOrganizationCount(activeOrganizationCount);
        response.setPartners(partners);
        response.setChannels(channelList);

        return response;
    }

    public Page<OrganizationCollaboration> findByOrganizationId(int page, int size) {
        var organizationId = Util.getOrgIdFromToken();
        return organizationCollaborationRepository.getAllBySenderOrganizationIdOrReceiverOrganizationIdOrderById(
                organizationId, organizationId, PageRequest.of(page, size));
    }

    public Page<OrganizationCollaboration> findByOrganizationIdManual(Long organizationId, int page, int size) {
        return organizationCollaborationRepository.getAllBySenderOrganizationIdOrReceiverOrganizationIdOrderById(
                organizationId, organizationId, PageRequest.of(page, size));
    }

    public Optional<OrganizationCollaboration> findLastOrganizationCollaborationById(long organizationId) {
        return organizationCollaborationRepository
                .findFirstBySenderOrganizationIdOrReceiverOrganizationIdOrderByLastUpdatedTimestampDesc(organizationId,
                        organizationId);
    }

    public OrganizationCollaboration updateIsViewed(Long orgCollaborationId) {
        var orgCollaboration = organizationCollaborationRepository.findById(orgCollaborationId);
        if (orgCollaboration.isPresent()) {
            var partnerships = orgCollaboration.get().getPartnershipMouVersions();
            partnerships.forEach(partnershipMouVersion -> {
                partnershipMouVersion.setViewed(true);
            });
            partnershipMouVersionRepository.saveAll(partnerships);
            orgCollaboration.get().setPartnershipMouVersions(partnerships);
            return orgCollaboration.get();
        } else {
            throw new ResourceNotFoundException(ErrorMessages.SH35);
        }
    }

    public OrganizationCollaboration updateIsEmailOpened(Long orgCollaborationId) {
        var orgCollaboration = organizationCollaborationRepository.findById(orgCollaborationId);
        if (orgCollaboration.isPresent()) {
            var partnerships = orgCollaboration.get().getPartnershipMouVersions();
            partnerships.forEach(partnershipMouVersion -> {
                partnershipMouVersion.setEmailOpened(true);
            });
            partnershipMouVersionRepository.saveAll(partnerships);
            orgCollaboration.get().setPartnershipMouVersions(partnerships);
            return orgCollaboration.get();
        } else {
            throw new ResourceNotFoundException(ErrorMessages.SH35);
        }
    }

    public OrganizationCollaboration updateIsEmailClicked(Long orgCollaborationId) {
        var orgCollaboration = organizationCollaborationRepository.findById(orgCollaborationId);
        if (orgCollaboration.isPresent()) {
            var partnerships = orgCollaboration.get().getPartnershipMouVersions();
            partnerships.forEach(partnershipMouVersion -> {
                partnershipMouVersion.setEmailClicked(true);
            });
            partnershipMouVersionRepository.saveAll(partnerships);
            orgCollaboration.get().setPartnershipMouVersions(partnerships);
            return orgCollaboration.get();
        } else {
            throw new ResourceNotFoundException(ErrorMessages.SH35);
        }
    }

    public List<TimelineEntity> getTimeline() {
        var orgCollaborationId = Util.getOrgIdFromToken();
        var timelineEntities = timelineRepository.getAllByOrganizationCollaborationId(orgCollaborationId);
        var meetings = meetingDetailsRepository.findByOrganizationCollaborationId(orgCollaborationId);
        timelineEntities.addAll(meetings.stream().map(meetingDetails -> {
            TimelineEntity timelineEntity = new TimelineEntity();
            timelineEntity.setAction(String.format("%s has scheduled a meeting", meetingDetails.getScheduledBy()));
            timelineEntity.setCreationTimestamp(meetingDetails.getCreationTimestamp());
            return timelineEntity;

        }).toList());
        var messages = organizationMessagesRepository
                .findAllByChatRoomIdOrderByCreationTimestampAsc(orgCollaborationId);
        if (messages.stream().findFirst().isPresent()) {
            var message = messages.stream().findFirst().get();
            TimelineEntity timelineEntity = new TimelineEntity();
            timelineEntity.setAction(String.format("%s has asked for clarification", message.getSenderId()));
            timelineEntity.setCreationTimestamp(message.getCreationTimestamp());
            timelineEntities.add(timelineEntity);
        }
        var data = timelineEntities.stream()
                .sorted(Comparator.comparing(TimelineEntity::getCreationTimestamp).reversed()).toList();
        return data;
    }

    public OrganizationCollaboration saveTimeline(Long orgCollaborationId, String template) {
        var data = organizationCollaborationRepository.findById(orgCollaborationId);
        if (data.isPresent()) {
            var organizationCollaboration = data.get();
            if ("partnership_initiated".equalsIgnoreCase(template)) {
                TimelineEntity timelineEntity = TimelineEntity.builder()
                        .organizationCollaborationId(organizationCollaboration.getId())
                        .action(String
                                .format("%s get notified over email",
                                        organizationRepository
                                                .findNameById(organizationCollaboration.getReceiverOrganizationId())))
                        .build();
                timelineRepository.save(timelineEntity);
            } else if ("Signer_Receiver".equalsIgnoreCase(template)) {
                TimelineEntity timelineEntity = TimelineEntity.builder()
                        .organizationCollaborationId(organizationCollaboration.getId())
                        .action(String
                                .format("%s gets notified over email for signing MOU",
                                        organizationRepository
                                                .findNameById(organizationCollaboration.getReceiverOrganizationId())))
                        .build();
                timelineRepository.save(timelineEntity);

            } else if ("Signer_Sender".equalsIgnoreCase(template)) {
                TimelineEntity timelineEntity = TimelineEntity.builder()
                        .organizationCollaborationId(organizationCollaboration.getId())
                        .action(String
                                .format("%s gets notified over email for signing MOU",
                                        organizationRepository
                                                .findNameById(organizationCollaboration.getSenderOrganizationId())))
                        .build();
                timelineRepository.save(timelineEntity);

            } else if ("Signer_Complete".equalsIgnoreCase(template)) {
                TimelineEntity timelineEntity = TimelineEntity.builder()
                        .organizationCollaborationId(organizationCollaboration.getId())
                        .action("partner valve room activated with no restrictions").build();
                timelineRepository.save(timelineEntity);
            }
        }
        return null;
    }

    public OrgCollaborationWithCredits getPartnerDashboard(int page, int size) {
        var organizationId = Util.getOrgIdFromToken();
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> data = organizationCollaborationRepository.getAllPartners(organizationId, pageable);

        // Map results to DTO
        List<PartnerResponse> dtos = data.getContent().stream()
                .map(result -> {
                    Long receiverId = (Long) result[0];
                    Long senderId = (Long) result[1];
                    Long id = (Long) result[2];
                    String status = (String) result[3];
                    Date creationTimestamp = (Date) result[4];

                    PartnershipType type = (receiverId.equals(organizationId)) ? PartnershipType.RECEIVER
                            : PartnershipType.SENDER;
                    Long oppositeId = receiverId.equals(organizationId) ? senderId : receiverId;

                    // Fetch organization details
                    var orgData = organizationRepository.getOrgNameAndDescriptionAndLogoUrl(oppositeId);

                    if (orgData != null) {
                        return new PartnerResponse(id, oppositeId, status, type, creationTimestamp, orgData.getName(),
                                orgData.getDescription(), orgData.getLogoUrl());
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        long validCount = dtos.size();
        var credits = creditsRepository.findByOrganizationId(organizationId);
        return new OrgCollaborationWithCredits(credits, new PageImpl<>(dtos, pageable, validCount));
    }

    public void scheduleMeetingWindow(Long organizationCollaborationId, Long senderId) {
        scheduler.schedule(() -> setMeetingWindow(organizationCollaborationId, senderId), 12, TimeUnit.HOURS);
    }

    private void setMeetingWindow(Long organizationCollaborationId, Long senderId) {
        TimelineEntity timelineEntity = TimelineEntity.builder()
                .organizationCollaborationId(organizationCollaborationId)
                .action(String.format("meeting window opened for %s", organizationRepository.findNameById(senderId)))
                .build();
        timelineRepository.save(timelineEntity);
    }

    public OfflineContract saveOfflineContract(String org1Email, String org2Email, MultipartFile document) {
        var link = uploadToS3(document, org1Email);
        return offlineContractRepository.save(OfflineContract.builder()
                .org1Email(org1Email)
                .org2Email(org2Email)
                .docLink(link).build());
    }

    private String uploadToS3(MultipartFile multipartFile, String email) {
        try {
            var s3Client = amazonS3Service.getS3Instance();
            String bucketName = "sharkdom.co.in";
            String key = "offlineContract" + env + "/" + email + "/" + multipartFile.getOriginalFilename(); // Object
            // key in S3
            s3Client.putObject(
                    new PutObjectRequest(bucketName, key, multipartFile.getInputStream(), new ObjectMetadata()));
            String fileUrl = s3Client.getUrl(bucketName, key).toString();
            log.info("File uploaded successfully to S3. File URL: {}", fileUrl);

            return fileUrl; // Return the URL of the uploaded file
        } catch (Exception e) {
            log.error("Exception occurred while uploading to S3", e);
            return null; // Handle the error appropriately in production
        }
    }

    public List<OfflineContract> getOfflineContract(String org1Email, String org2Email) {
        return offlineContractRepository.getAllByOrg1EmailAndOrg2Email(org1Email, org2Email);
    }

    public Optional<MouHistory> getMouHistory(Long organizationCollaborationId, Long organizationId) {
        return mouHistoryRepository.findByOrganizationIdAndOrganizationCollaborationId(organizationId,
                organizationCollaborationId);
    }

    public Optional<EnvelopeEntity> getEnvelopeById(String envelopeId) {
        return envelopeRepository.findByEnvelopeId(envelopeId);
    }

    public List<MouHistory> getAllPendingMou() {
        var organizationId = Util.getOrgIdFromToken();
        var history = mouHistoryRepository.findAllByOrganizationIdAndSigned(organizationId, false);
        history.forEach(mouHistory -> mouHistory
                .setOrganizationName(organizationRepository.findNameById(mouHistory.getOrganizationId())));
        return history;
    }

    public void signMou(MouSignRequest mouSignRequest, MultipartFile pdf) {
        organizationCollaborationRepository.findById(mouSignRequest.organizationCollaborationId())
                .ifPresent(organizationCollaboration -> {
                    var optionalMouHistory = mouHistoryRepository.findByOrganizationIdAndOrganizationCollaborationId(
                            mouSignRequest.organizationId(), mouSignRequest.organizationCollaborationId());
                    Optional<EnvelopeEntity> optionalEnvelopeEntity = envelopeRepository
                            .findByEnvelopeId(mouSignRequest.envelopeId());
                    if (optionalEnvelopeEntity.isPresent() && optionalMouHistory.isPresent()) {
                        var mouHistory = optionalMouHistory.get();
                        var envelope = optionalEnvelopeEntity.get();
                        if (mouSignRequest.organizationId()
                                .equals(organizationCollaboration.getReceiverOrganizationId())) {
                            try {
                                var filePath = "/" + env + "/" + organizationCollaboration.getId() + "/"
                                        + organizationCollaboration.getSenderOrganizationId() + "/" + pdf.getName()
                                        + ".pdf";
                                var pdfLink = azureStorageService.uploadFile(pdf.getInputStream(), filePath);
                                var mouHistorySender = MouHistory.builder()
                                        .organizationCollaborationId(mouSignRequest.organizationCollaborationId())
                                        .organizationId(organizationCollaboration.getSenderOrganizationId())
                                        .pdfUrl(filePath)
                                        .type(Flag.SENDER)
                                        .signed(false)
                                        .envelopeId(mouSignRequest.envelopeId()).build();
                                PartnershipMouVersion partnershipMouVersion = organizationCollaboration
                                        .getPartnershipMouVersions()
                                        .get(organizationCollaboration.getPartnershipMouVersions().size() - 1);
                                envelope.setStatus("PARTIALLY_COMPLETED");
                                envelope.setHolder(organizationRepository
                                        .findNameById(organizationCollaboration.getSenderOrganizationId()));
                                envelope.setSignedByReceiver(true);
                                envelope.setDateCreated(LocalDate.now());
                                envelope.setStatusDate(LocalDate.now());
                                envelopeRepository.save(envelope);
                                mouHistoryRepository.save(mouHistorySender);
                                partnershipMouVersion.setStatus(MouStatus.PENDING_SENDER);
                                partnershipMouVersion.setReceiverSignedOn(DateTime.now().toDate());
                                partnershipMouVersionRepository.save(partnershipMouVersion);
                                sendMouNotification(
                                        organizationRepository
                                                .findById(organizationCollaboration.getSenderOrganizationId()).get(),
                                        organizationRepository
                                                .findById(organizationCollaboration.getReceiverOrganizationId()).get());
                                String docUrl;
                                if (env.equals("dev")) {
                                    docUrl = "https://dev.sharkdom.com/mou/" + organizationCollaboration.getId()
                                            + "/sign";
                                } else {
                                    docUrl = "https://sharkdom.com/mou/" + organizationCollaboration.getId() + "/sign";
                                }
                                emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel
                                        .builder()
                                        .organizationIds(List.of(organizationCollaboration.getSenderOrganizationId()))
                                        .templateCode("Signer_Sender")
                                        .senderOrganizationName(organizationRepository
                                                .findNameById(organizationCollaboration.getReceiverOrganizationId()))
                                        .organizationName(organizationRepository
                                                .findNameById(organizationCollaboration.getSenderOrganizationId()))
                                        .docUrl(docUrl).build(), null, organizationCollaboration.getId(), 1L);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        } else {
                            try {
                                var filePath = "/" + env + "/" + organizationCollaboration.getId() + "/" + pdf.getName()
                                        + ".pdf";
                                var pdfLink = azureStorageService.uploadFile(pdf.getInputStream(), filePath);
                                PartnershipMouVersion partnershipMouVersion = organizationCollaboration
                                        .getPartnershipMouVersions()
                                        .get(organizationCollaboration.getPartnershipMouVersions().size() - 1);
                                envelope.setStatus("COMPLETED");
                                envelope.setSignedBySender(true);
                                envelope.setStatusDate(LocalDate.now());
                                envelopeRepository.save(envelope);
                                partnershipMouVersion.setStatus(MouStatus.ACTIVE);
                                partnershipMouVersion.setFilePath(filePath);
                                partnershipMouVersion.setSenderSignedOn(DateTime.now().toDate());
                                partnershipMouVersionRepository.save(partnershipMouVersion);
                                String docUrl;
                                if (env.equals("dev")) {
                                    docUrl = "https://dev.sharkdom.com/mou/" + organizationCollaboration.getId()
                                            + "/sign";
                                } else {
                                    docUrl = "https://sharkdom.com/mou/" + organizationCollaboration.getId() + "/sign";
                                }
                                emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel
                                                .builder()
                                                .organizationIds(List.of(organizationCollaboration.getSenderOrganizationId()))
                                                .organizationName(organizationRepository
                                                        .findNameById(organizationCollaboration.getSenderOrganizationId()))
                                                .docUrl(docUrl)
                                                .templateCode("Signer_Complete").build(), null,
                                        organizationCollaboration.getId(), 1L);

                                emailService.sendByTemplateAndOrganizationIds(
                                        TemplateOrganizationEmailReqModel.builder()
                                                .organizationIds(
                                                        List.of(organizationCollaboration.getReceiverOrganizationId()))
                                                .docUrl(docUrl)
                                                .organizationName(organizationRepository.findNameById(
                                                        organizationCollaboration.getReceiverOrganizationId()))
                                                .templateCode("Signer_Complete").build(),
                                        null, organizationCollaboration.getId(), 1L);
                                var senderName = organizationRepository
                                        .findNameById(organizationCollaboration.getSenderOrganizationId());
                                var receiverName = organizationRepository
                                        .findNameById(organizationCollaboration.getReceiverOrganizationId());
                                createProposalAcceptNotification(organizationCollaboration.getReceiverOrganizationId(),
                                        senderName, receiverName);
                                createProposalAcceptNotification(organizationCollaboration.getSenderOrganizationId(),
                                        senderName, receiverName);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        mouHistory.setSigned(true);
                        mouHistoryRepository.save(mouHistory);
                    }
                });
    }

    private void createProposalAcceptNotification(Long organizationId, String senderName, String receiverName) {
        var notification = Notification.builder()
                .subject("Official Announcement: Partnership Started")
                .body(String.format(
                        "Congratulation! Your Partnership has been accepted. We are excited to announced partnership among %s X %s is started on %s.",
                        senderName, receiverName, LocalDate.now()))
                .forWeb(true)
                .organizationId(organizationId)
                .build();
        webSocketHandler.sendMessageToUser(organizationId, notification);
        notificationRepository.save(notification);
    }

    private void sendMouNotification(Organization receiverOrganization, Organization senderOrganization) {
        Notification notification = Notification.builder()
                .subject("MOU Signing Required")
                .body(String.format(
                        " %s has sent you a MOU to review and sign. Please check your email for the Memorandum of Understanding (MOU) and follow the instructions to complete the signing process.",
                        senderOrganization.getName()))
                .forWeb(true)
                .organizationId(receiverOrganization.getId())
                .build();
        webSocketHandler.sendMessageToUser(receiverOrganization.getId(), notification);

        Notification notificationSender = Notification.builder()
                .subject("MOU Signing Required")
                .body(String.format(" You have sent a MOU for review and sign to %s.", receiverOrganization.getName()))
                .forWeb(true)
                .organizationId(senderOrganization.getId())
                .build();
        webSocketHandler.sendMessageToUser(senderOrganization.getId(), notificationSender);
        notificationServiceNote.create(notification);
        notificationServiceNote.create(notificationSender);

    }

    public List<OrganizationCollaborationCategoryEntity> createCollaborationCategory(
            CollaborationCategoryRequest request) {
        List<OrganizationCollaborationCategoryEntity> toSave = new ArrayList<>();
        for (Long collabId : request.getOrganizationCollaborationId()) {
            var existing = organizationCollaborationCategoryRepository
                    .findByOrganizationIdAndOrganizationCollaborationIdAndCategory(
                            request.getOrganizationId(), collabId);

            if (existing.isPresent()) {
                OrganizationCollaborationCategoryEntity patchEntity = existing.get();
                patchEntity.setCategory(request.getCategory());
                toSave.add(patchEntity); // or update fields
            } else {
                var entity = new OrganizationCollaborationCategoryEntity();
                entity.setOrganizationId(request.getOrganizationId());
                entity.setOrganizationCollaborationId(collabId);
                entity.setCategory(request.getCategory());
                toSave.add(entity);
            }
        }
        return organizationCollaborationCategoryRepository.saveAll(toSave);
    }

    public CollaborationCategory getCollaborationCategory(Long partnerId) {
        var organizationId = Util.getOrgIdFromToken();
        var collaboration = organizationCollaborationRepository.findBySenderOrganizationIdOrReceiverOrganizationId(organizationId, partnerId);
        if (collaboration != null) {
            return organizationCollaborationCategoryRepository.findCategoryByOrganizationCollaborationIdAndOrganizationId(collaboration.getId(), organizationId);
        }
        return CollaborationCategory.RELIABLE_PARTNER;
    }

    public CollaborationCategory getCollaborationCategoryV1(Long orgId,Long partnerId) {
        var organizationId = orgId;
        var collaboration = organizationCollaborationRepository.findBySenderOrganizationIdOrReceiverOrganizationId(organizationId, partnerId);
        if (collaboration != null) {
            return organizationCollaborationCategoryRepository.findCategoryByOrganizationCollaborationIdAndOrganizationId(collaboration.getId(), organizationId);
        }
        return CollaborationCategory.RELIABLE_PARTNER;
    }

    public Page<PartnerDashboardResponse> getAllCollaborations(CollaborationStatus status,
                                                               int page, int size) {
        var organizationId = Util.getOrgIdFromToken();
        var pageable = PageRequest.of(page, size);
        var senderCollaborations = organizationCollaborationRepository
                .getPartnerDashboardAllBySenderOrganizationId(organizationId);
        var receiverCollaborations = organizationCollaborationRepository
                .getPartnerDashboardAllByReceiverOrganizationId(organizationId);
        List<CollaborationRepositoryResponse> allCollaborations = new ArrayList<>();
        allCollaborations.addAll(senderCollaborations);
        allCollaborations.addAll(receiverCollaborations);
        List<CollaborationRepositoryResponse> filteredCollaborations = allCollaborations.stream()
                .filter(collaboration -> switch (status) {
                    case SENT ->
                            "PENDING".equals(collaboration.getStatus()) && senderCollaborations.contains(collaboration);
                    case RECEIVED ->
                            "PENDING".equals(collaboration.getStatus()) && receiverCollaborations.contains(collaboration);
                    case ACTIVE -> "ACTIVE".equals(collaboration.getStatus());
                    case REJECTED -> "REJECTED".equals(collaboration.getStatus());
                    default -> true;
                })
                .toList();
        List<PartnerDashboardResponse> responses = filteredCollaborations.stream()
                .map(res -> fetchPartnerDashboardResponse(res, organizationId))
                .toList();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());

        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    private PartnerDashboardResponse fetchPartnerDashboardResponse(CollaborationRepositoryResponse response,
                                                                   Long organizationId) {
        // Fetch additional details based on organizationId
        Organization organizationDetails = organizationRepository.findById(response.getOrganizationId())
                .orElse(new Organization());

        var collaborationCategory = organizationCollaborationCategoryRepository
                .findCategoryByOrganizationCollaborationIdAndOrganizationId(response.getId(), organizationId);
        // Populate PartnerDashboardResponse
        return new PartnerDashboardResponse(
                response.getId(),
                organizationDetails.getName(),
                organizationDetails.getLogoUrl(),
                collaborationCategory,
                response.getCreationTimestamp(),
                response.getStatus(),
                response.getOrganizationId());
    }

    @Transactional
    public PartnerSpaceRoom createPartnerSpaceRoom(PartnerSpaceRequest request) {

        Long creatorOrgId = request.getCreatorOrgId();

        if (creatorOrgId == null) {
            throw new IllegalArgumentException("Creator organizationId must be provided");
        }

        PartnerSpaceRoom room = PartnerSpaceRoom.builder()
                .spaceName(request.getSpaceName())
                .spaceType(request.getSpaceType())
                .partnerCreated(creatorOrgId)
                .partnerJoined(request.getPartnerJoined())
                .build();

        room = partnerSpaceRepository.save(room);
        room.setChatRoomId(room.getId());
        return partnerSpaceRepository.save(room);

    }

    public List<PartnerSpaceRoom> getPartnerSpaceByOrganizationId() {
        Long organizationId = Util.getOrgIdFromToken();
        Integer orgIdInt = organizationId != null ? organizationId.intValue() : null;
        return partnerSpaceRepository.findAllByOrganizationId(organizationId, orgIdInt);
    }

    public void sendCampaignCreatedMessage(Long organizationId, Long partnerId) {
        var collaboration = organizationCollaborationRepository.findBySenderOrganizationIdOrReceiverOrganizationId(organizationId, partnerId);
        if (Objects.nonNull(collaboration)) {
            SendMessageRequest sendMessageRequest = new SendMessageRequest(
                    collaboration.getId(),
                    "Referral Program has been shared",
                    organizationId,
                    LinkerType.EMPTY,
                    Flag.ASSISTANT,
                    organizationId,
                    partnerId
            );
            sendMessage(sendMessageRequest);
        }
    }

    private ChannelFlag getChannelCategory(String message) {
        String url = "https://sharkdomchatsegmentation-b4f8hzgdanh7ggcu.centralindia-01.azurewebsites.net/categorize";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("text", message);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestMap, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        // Return the response body
        return ChannelFlag.values()[(int) response.getBody().get("category")];
    }

    @Transactional
    public void updateProposalDetails(Long collaborationId, ProposalEditRequest request) {
        var optionalCollaboration = organizationCollaborationRepository.findById(collaborationId);
        if (optionalCollaboration.isPresent()) {
            var collaboration = optionalCollaboration.get();
            if (collaboration.getStatus().equals("PENDING")) {
                request.receiverBenefits().forEach(benefit -> {
                    var editEntity = ProposalEditHistoryMapper.mapToProposalEditHistoryEntity(collaborationId, benefit, collaboration.getSenderOrganizationId(), collaboration.getReceiverOrganizationId(), LinkerType.RECEIVER_BENEFITS);
                    proposalEditHistoryRepository.save(editEntity);
                });
                request.senderBenefits().forEach(benefit -> {
                    var editEntity = ProposalEditHistoryMapper.mapToProposalEditHistoryEntity(collaborationId, benefit, collaboration.getSenderOrganizationId(), collaboration.getReceiverOrganizationId(), LinkerType.SENDER_BENEFITS);
                    proposalEditHistoryRepository.save(editEntity);
                });

            }
        }
    }

    @Transactional
    public void updateProposalEditStatus(Long historyId, EditHistoryStatus status) {
        var optionalHistory = proposalEditHistoryRepository.findById(historyId);
        if (optionalHistory.isPresent()) {
            var history = optionalHistory.get();
            if (EditHistoryStatus.REJECTED.equals(status)) {
                history.setHistoryStatus(status);
                proposalEditHistoryRepository.save(history);
            } else if (EditHistoryStatus.ACCEPTED.equals(status)) {
                history.setHistoryStatus(status);
                proposalEditHistoryRepository.save(history);
                applyAcceptedChanges(history);
            }
        }
    }

    private void applyAcceptedChanges(ProposalEditHistoryEntity history) {
        switch (history.getMode()) {
            case EDIT:
                updateOriginalBenefitStatus(history, BenefitsStatus.EDITED);
                createNewBenefit(history);
                break;
            case ADD:
                createNewBenefit(history);
                break;
            case DELETE:
                updateOriginalBenefitStatus(history, BenefitsStatus.DELETED);
                break;
        }
    }

    private void updateOriginalBenefitStatus(ProposalEditHistoryEntity history, BenefitsStatus status) {
        if (history.getOriginalBenefitId() == null) {
            return;
        }

        if (LinkerType.SENDER_BENEFITS.equals(history.getLinkerType())) {
            var originalBenefit = senderBenefitRepository.findById(history.getOriginalBenefitId());
            if (originalBenefit.isPresent()) {
                var benefit = originalBenefit.get();
                benefit.setStatus(status);
                senderBenefitRepository.save(benefit);
            }
        } else if (LinkerType.RECEIVER_BENEFITS.equals(history.getLinkerType())) {
            var originalBenefit = receiverBenefitRepository.findById(history.getOriginalBenefitId());
            if (originalBenefit.isPresent()) {
                var benefit = originalBenefit.get();
                benefit.setStatus(status);
                receiverBenefitRepository.save(benefit);
            }
        }
    }

    private void createNewBenefit(ProposalEditHistoryEntity history) {
        if (LinkerType.SENDER_BENEFITS.equals(history.getLinkerType())) {
            SenderBenefit newBenefit = new SenderBenefit();
            newBenefit.setBenefit(history.getBenefit());
            newBenefit.setDescription(history.getDescription());
            newBenefit.setStatus(BenefitsStatus.ACTIVE);
            var savedBenefit = senderBenefitRepository.save(newBenefit);
            organizationCollaborationRepository.findById(history.getOrganizationCollaborationId()).ifPresent(collaboration -> collaboration.getPartnershipMouVersions().stream()
                    .findFirst()
                    .ifPresent(mouVersion -> {
                        mouVersion.getSenderBenefits().add(savedBenefit);
                        organizationCollaborationRepository.save(collaboration);
                    }));
        } else if (LinkerType.RECEIVER_BENEFITS.equals(history.getLinkerType())) {
            ReceiverBenefit newBenefit = new ReceiverBenefit();
            newBenefit.setBenefit(history.getBenefit());
            newBenefit.setDescription(history.getDescription());
            newBenefit.setStatus(BenefitsStatus.ACTIVE);
            var savedBenefit = receiverBenefitRepository.save(newBenefit);
            organizationCollaborationRepository.findById(history.getOrganizationCollaborationId()).ifPresent(collaboration -> collaboration.getPartnershipMouVersions().stream()
                    .findFirst()
                    .ifPresent(mouVersion -> {
                        mouVersion.getReceiverBenefits().add(savedBenefit);
                        organizationCollaborationRepository.save(collaboration);
                    }));
        }
    }

    public ResponseEntity<List<List<ProposalEditHistoryEntity>>> getProposalEditHistory(Long collaborationId) {
        List<ProposalEditHistoryEntity> allHistories = proposalEditHistoryRepository
                .findByOrganizationCollaborationIdOrderByCreationTimestampDesc(collaborationId);
        Map<Long, List<ProposalEditHistoryEntity>> historyGroups = new HashMap<>();
        List<ProposalEditHistoryEntity> rootHistories = new ArrayList<>();
        for (ProposalEditHistoryEntity history : allHistories) {
            if (history.getParentId() == null) {
                rootHistories.add(history);
            } else {
                historyGroups.computeIfAbsent(history.getParentId(), k -> new ArrayList<>())
                        .add(history);
            }
        }
        List<List<ProposalEditHistoryEntity>> response = new ArrayList<>();

        for (ProposalEditHistoryEntity root : rootHistories) {
            List<ProposalEditHistoryEntity> group = new ArrayList<>();
            group.add(root);

            // Add any children
            List<ProposalEditHistoryEntity> children = historyGroups.getOrDefault(root.getId(), Collections.emptyList());
            children.stream()
                    .sorted(Comparator.comparing(BaseEntity::getCreationTimestamp).reversed())
                    .forEach(group::add);

            response.add(group);
        }
        response.sort((g1, g2) -> g2.get(0).getCreationTimestamp().compareTo(g1.get(0).getCreationTimestamp()));

        return ResponseEntity.ok(response);
    }

    public void checkPartnerSpaceExists(Long collaborationId) {
        Optional<OrganizationCollaboration> collaborationOpt = organizationCollaborationRepository.findById(collaborationId);

        if (collaborationOpt.isEmpty()) {
            throw new ServiceException(ErrorMessages.SH09, collaborationId);
        }
        if (!organizationMessagesRepository.existsByChatRoomId(collaborationId)) {
            SendMessageRequest sendMessageRequest = new SendMessageRequest(collaborationId, "Welcome to Sharkdom", -1L, LinkerType.EMPTY, Flag.ASSISTANT, collaborationOpt.get().getSenderOrganizationId(), collaborationOpt.get().getReceiverOrganizationId());
            sendMessage(sendMessageRequest);
        }

    }

    public BenefitsResponse getBenefitsDetails(Long benefitId, LinkerType linkerType) {
        if (LinkerType.SENDER_BENEFITS.equals(linkerType)) {
            Optional<SenderBenefit> senderBenefit = senderBenefitRepository.findById(benefitId);
            if (senderBenefit.isPresent()) {
                return new BenefitsResponse(senderBenefit.get().getId(), senderBenefit.get().getBenefit(), senderBenefit.get().getDescription());
            }
        } else if (LinkerType.RECEIVER_BENEFITS.equals(linkerType)) {
            Optional<ReceiverBenefit> receiverBenefit = receiverBenefitRepository.findById(benefitId);
            if (receiverBenefit.isPresent()) {
                return new BenefitsResponse(receiverBenefit.get().getId(), receiverBenefit.get().getBenefit(), receiverBenefit.get().getDescription());
            }
        }
        return null;
    }

    public List<ZohoSignedDocumentEntity> getZohoDocuments(Long organizationIdCollaborationId) {
        return zohoSignedDocumentRepository.findAllByOrganizationCollaborationId(organizationIdCollaborationId);
    }

    public long getAllCollaborationsCount(CollaborationStatus status) {
        var organizationId = Util.getOrgIdFromToken();
        var senderCollaborations = organizationCollaborationRepository
                .getPartnerDashboardAllBySenderOrganizationId(organizationId);
        var receiverCollaborations = organizationCollaborationRepository
                .getPartnerDashboardAllByReceiverOrganizationId(organizationId);

        List<CollaborationRepositoryResponse> allCollaborations = new ArrayList<>();
        allCollaborations.addAll(senderCollaborations);
        allCollaborations.addAll(receiverCollaborations);

        return allCollaborations.stream()
                .filter(collaboration -> switch (status) {
                    case SENT ->
                            "PENDING".equals(collaboration.getStatus()) && senderCollaborations.contains(collaboration);
                    case RECEIVED ->
                            "PENDING".equals(collaboration.getStatus()) && receiverCollaborations.contains(collaboration);
                    case ACTIVE -> "ACTIVE".equals(collaboration.getStatus());
                    case REJECTED -> "REJECTED".equals(collaboration.getStatus());
                    default -> true;
                })
                .count();
    }

    public List<PartnerDashboardResponse> getAllCollaborations(CollaborationStatus status) {
        var organizationId = Util.getOrgIdFromToken();

        var senderCollaborations = organizationCollaborationRepository
                .getPartnerDashboardAllBySenderOrganizationId(organizationId);
        var receiverCollaborations = organizationCollaborationRepository
                .getPartnerDashboardAllByReceiverOrganizationId(organizationId);

        List<CollaborationRepositoryResponse> allCollaborations = new ArrayList<>();
        allCollaborations.addAll(senderCollaborations);
        allCollaborations.addAll(receiverCollaborations);

        List<CollaborationRepositoryResponse> filteredCollaborations = allCollaborations.stream()
                .filter(collaboration -> switch (status) {
                    case SENT ->
                            "PENDING".equals(collaboration.getStatus()) && senderCollaborations.contains(collaboration);
                    case RECEIVED ->
                            "PENDING".equals(collaboration.getStatus()) && receiverCollaborations.contains(collaboration);
                    case ACTIVE -> "ACTIVE".equals(collaboration.getStatus());
                    case REJECTED -> "REJECTED".equals(collaboration.getStatus());
                    default -> true;
                })
                .toList();
        return filteredCollaborations.stream()
                .map(res -> fetchPartnerDashboardResponse(res, organizationId))
                .toList();
    }

    public List<OrganizationCollaboration> getAllCollaboration() {
        var organizationId = Util.getOrgIdFromToken();
        var senderCollaborations = organizationCollaborationRepository
                .getPartnerDashboardAllBySenderOrganization(organizationId);
        var receiverCollaborations = organizationCollaborationRepository
                .getPartnerDashboardAllByReceiverOrganization(organizationId);
        List<OrganizationCollaboration> allCollaborations = new ArrayList<>();
        allCollaborations.addAll(senderCollaborations);
        allCollaborations.addAll(receiverCollaborations);
        return allCollaborations;
    }

    @Transactional
    public MyPartnerAssignment saveMyPartnerAssignment(SaveAssignmentDto saveAssignmentDto) {
        Long orgId = Util.getOrgIdFromToken();
        Long partnerOrgId = saveAssignmentDto.getPartnerOrgId();
        String userId = saveAssignmentDto.getUserId();

        log.info("Saving MyPartnerAssignment - orgId: {}, partnerOrgId: {}, userId: {}", orgId, partnerOrgId, userId);

        // Check existing record (Upsert logic)
        MyPartnerAssignment existingAssignment = myPartnerAssignmentRepository
                .findByOrganizationIdAndPartnerOrgId(orgId, partnerOrgId)
                .orElse(null);

        if (existingAssignment != null) {
            log.info("Existing assignment found for orgId: {} and partnerOrgId: {}. Updating userId to: {}", orgId, partnerOrgId, userId);
            existingAssignment.setUserId(userId);
            return myPartnerAssignmentRepository.save(existingAssignment);
        }

        log.info("No existing assignment found. Creating new assignment for orgId: {} and partnerOrgId: {}", orgId, partnerOrgId);

        MyPartnerAssignment newAssignment = new MyPartnerAssignment();
        newAssignment.setOrganizationId(orgId);
        newAssignment.setPartnerOrgId(partnerOrgId);
        newAssignment.setUserId(userId);

        return myPartnerAssignmentRepository.save(newAssignment);
    }

    public MyPartnerAssignment getMyPartnerAssignment(Long partnerOrgId) {
        Long orgId = Util.getOrgIdFromToken();
        log.info("Fetching MyPartnerAssignment for orgId: {} and partnerOrgId: {}", orgId, partnerOrgId);

        return myPartnerAssignmentRepository.findByOrganizationIdAndPartnerOrgId(orgId, partnerOrgId)
                .orElseThrow(() -> {
                    log.warn("No MyPartnerAssignment found for orgId: {} and partnerOrgId: {}", orgId, partnerOrgId);
                    return new RuntimeException("Assignment not found for given organization and partner");
                });
    }
}
