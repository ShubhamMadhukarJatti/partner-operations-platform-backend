package com.sharkdom.service.referral;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.sharkdom.config.WebSocketHandler;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.Flag;
import com.sharkdom.constants.LinkerType;
import com.sharkdom.constants.campaign.CampaignStatus;
import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.entity.email.EmailVerification;
import com.sharkdom.entity.notification.Notification;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organizationcollaboration.OrganizationCollaboration;
import com.sharkdom.entity.referral.*;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.model.email.TemplateOrganizationEmailReqModel;
import com.sharkdom.model.organizatiocollaboration.SendMessageRequest;
import com.sharkdom.model.paymenttracking.RazorpayPaymentDetailsDto;
import com.sharkdom.model.referral.*;
import com.sharkdom.repository.email.EmailVerificationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationCollaborationRepository;
import com.sharkdom.repository.organizationcollaboration.TimelineRepository;
import com.sharkdom.repository.paymenttracking.RazorpayTrackingRepository;
import com.sharkdom.repository.paymenttracking.RazorpayTrackingTestRepository;
import com.sharkdom.repository.referral.*;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.notification.NotificationService;
import com.sharkdom.service.organizationcollaboration.OrganizationCollaborationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.sharkdom.util.GeneralUtils.*;


@Service
@Slf4j
public class ReferralService {
    @Value("${app.environment.proxy_url}")
    private String baseUrl;
    @Value("${api-url}")
    private String apiUrl;
    private final ReferralRepository referralRepository;
    private final ImpressionsRepository impressionsRepository;
    private final LeadsRepository leadsRepository;
    private final ReferralWhitelistRepository referralWhitelistRepository;
    private final OrganizationRepository organizationRepository;
    private final CampaignRepository campaignRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final OrganizationCollaborationRepository organizationCollaborationRepository;
    private final RazorpayTrackingTestRepository razorpayTrackingTestRepository;
    private final RazorpayTrackingRepository razorpayTrackingRepository;
    private final OrganizationCollaborationService organizationCollaborationService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final TimelineRepository timelineRepository;
    private final CampaignStatsRepository campaignStatsRepository;
    private final InviteCampaignRepository inviteCampaignRepository;
    private final NotificationService notificationServiceNote;
    private final WebSocketHandler webSocketHandler;
    private final OrganizationUserMappingRepository organizationUserMappingRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public ReferralService(ReferralRepository referralRepository, ImpressionsRepository impressionsRepository, LeadsRepository leadsRepository, ReferralWhitelistRepository referralWhitelistRepository, OrganizationRepository organizationRepository, CampaignRepository campaignRepository, EmailVerificationRepository emailVerificationRepository, OrganizationCollaborationRepository organizationCollaborationRepository, RazorpayTrackingTestRepository razorpayTrackingTestRepository, RazorpayTrackingRepository razorpayTrackingRepository, OrganizationCollaborationService organizationCollaborationService, EmailService emailService, ObjectMapper objectMapper, TimelineRepository timelineRepository, CampaignStatsRepository campaignStatsRepository, InviteCampaignRepository inviteCampaignRepository, NotificationService notificationServiceNote, WebSocketHandler webSocketHandler, OrganizationUserMappingRepository organizationUserMappingRepository, UserRepository userRepository) {
        this.referralRepository = referralRepository;
        this.impressionsRepository = impressionsRepository;
        this.leadsRepository = leadsRepository;
        this.referralWhitelistRepository = referralWhitelistRepository;
        this.organizationRepository = organizationRepository;
        this.campaignRepository = campaignRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.organizationCollaborationRepository = organizationCollaborationRepository;
        this.razorpayTrackingTestRepository = razorpayTrackingTestRepository;
        this.razorpayTrackingRepository = razorpayTrackingRepository;
        this.organizationCollaborationService = organizationCollaborationService;
        this.emailService = emailService;
        this.objectMapper = objectMapper;
        this.timelineRepository = timelineRepository;
        this.campaignStatsRepository = campaignStatsRepository;
        this.inviteCampaignRepository = inviteCampaignRepository;
        this.notificationServiceNote = notificationServiceNote;
        this.webSocketHandler = webSocketHandler;
        this.organizationUserMappingRepository = organizationUserMappingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReferralLinkResponse generateReferralLink(Long organizationId, String landingPage) {
        var organization = organizationRepository.findById(organizationId);
        if (organization.isPresent()) {
            String referralCode = RandomStringUtils.random(8, true, true);
            ReferralEntity referralEntity = ReferralEntity.builder().referralCode(referralCode).organizationId(organizationId).build();
            String referralLink = landingPage + "?partner_id=" + referralCode;

            if (referralWhitelistRepository.findByDomain(organization.get().getDomain()).isEmpty()) {
                ReferralWhitelist referralWhitelist = ReferralWhitelist.builder().domain(organization.get().getDomain()).build();
                referralWhitelistRepository.save(referralWhitelist);
            }
            /*List<Long> collabIds = organizationCollaborationRepository.getAllCollaborations(organizationId);
            collabIds.forEach(id -> {
                var orgCollab = organizationCollaborationRepository.findById(id);
                if (orgCollab.isPresent()) {
                    TimelineEntity timelineEntity = TimelineEntity.builder()
                            .organizationCollaborationId(id)
                            .action(String.format("%s shared a referral link", organizationRepository.findNameById(organizationId))).build();
                    timelineRepository.save(timelineEntity);
                }
            });*/
            var prodUrl = String.format("%stracking/razorpay/%s", apiUrl, organization.get().getCode());
            var testUrl = String.format("%stracking/razorpay/test/%s", apiUrl, organization.get().getCode());
            referralEntity.setTestWebhookUrl(testUrl);
            referralEntity.setProdWebhookUrl(prodUrl);
            referralRepository.save(referralEntity);
            return ReferralLinkResponse.builder()
                    .referralCode(referralCode)
                    .referralLink(referralLink)
                    .prodWebhookUrl(prodUrl)
                    .testWebhookUrl(testUrl)
                    .build();
        } else {
            return ReferralLinkResponse.builder().build();
        }
    }

    @Transactional
    public void saveImpression(String referralCode, String ip) {
        if (referralRepository.existByReferralCode(referralCode) != 0) {
            ImpressionEntity impressionEntity = ImpressionEntity.builder()
                    .ipAddress(ip)
                    .referralCode(referralCode)
                    .build();
            impressionsRepository.save(impressionEntity);
        }
    }


    @Transactional
    public void saveLead(String referralCode, String email, String name) {

        if (referralRepository.existByReferralCode(referralCode) != 0) {
            LeadsEntity leadsEntity = LeadsEntity.builder()
                    .referralCode(referralCode)
                    .email(email)
                    .name(name)
                    .build();
            leadsRepository.save(leadsEntity);
        }
    }

    @Transactional
    public LeadsEntity updateLeadStatus(UpdateLeadStatus updateLeadStatus) {
        var optionalLeads = leadsRepository.findByEmailAndReferralCode(updateLeadStatus.email(), updateLeadStatus.referralCode());
        if (optionalLeads.isPresent()) {
            var lead = optionalLeads.get();
            lead.setLeadsStatus(updateLeadStatus.leadsStatus());
            if (updateLeadStatus.leadsStatus().equals(LeadsEntity.LeadsStatus.CONNECTED)) {
                var campaign = campaignRepository.findByReferralCode(updateLeadStatus.referralCode());
                if (Objects.nonNull(campaign)) {
                    var senderOrgUser = organizationUserMappingRepository.findByOrganizationIdAndRole(campaign.getOrganizationId(), OrgUserRole.ADMIN);
                    if (!senderOrgUser.isEmpty()) {
                        sendPartnerSpaceMessage(campaign.getOrganizationId(), campaign.getPartnerId(), String.format("%s from %s has marked your 1 lead as qualified", senderOrgUser.get(0).getUser().getName(), organizationRepository.findNameById(senderOrgUser.get(0).getOrganizationUserMapping().getOrganizationId())));
                    }
                    var receiverOrgUser = organizationUserMappingRepository.findByOrganizationIdAndRole(campaign.getPartnerId(), OrgUserRole.ADMIN);
                    if (!receiverOrgUser.isEmpty()) {
                        sendPartnerSpaceMessage(campaign.getOrganizationId(), campaign.getPartnerId(), String.format("%s from your team has marked 1 lead as qualified", receiverOrgUser.get(0).getUser().getName()));
                    }

                }
            }
            return leadsRepository.save(lead);
        }
        throw new SharkdomException(ErrorMessages.SH118, updateLeadStatus.email());
    }


    @Transactional
    public List<ReferralData> getReferralData(String referralCode, String from, String to) {
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);

        // Validate date range
        if (fromDate.isAfter(toDate)) {
            return Collections.emptyList();
        }

        // Check if referral code exists
        Long organizationId = referralRepository.existByReferralCode(referralCode);
        if (organizationId == null || organizationId == 0) {
            return Collections.emptyList();
        }

        // Calculate total days and days per group
        long totalDays = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        int daysPerGroup = (int) Math.ceil((double) totalDays / 7);

        List<ReferralData> timeSeriesData = new ArrayList<>();

        // Initialize previous group metrics
        long prevUniqueImpressions = 0;
        long prevTotalImpressions = 0;
        long prevLeadsCount = 0;
        double prevConversionRate = 0;
        double prevRevenue = 0;

        // Generate time series data
        for (int i = 0; i < 7; i++) {
            LocalDate groupStartDate = fromDate.plusDays((long) i * daysPerGroup);
            LocalDate groupEndDate = groupStartDate.plusDays(daysPerGroup - 1);
            if (groupEndDate.isAfter(toDate)) {
                groupEndDate = toDate;
            }
            if (groupStartDate.isAfter(toDate)) {
                break;
            }

            // Calculate current group metrics
            long uniqueImpressions = impressionsRepository.distinctCountByReferralCode(referralCode, groupStartDate, groupEndDate).size();
            long periodImpressions = impressionsRepository.countImpressionsByReferralCode(referralCode, groupStartDate, groupEndDate).size();
            List<LeadsInfo> leadsInfo = leadsRepository.findAllByReferralCode(referralCode, groupStartDate, groupEndDate);
            var leadsCount = leadsInfo.size();
            var conversions = leadsInfo.stream().filter(lead -> LeadsEntity.LeadsStatus.CONNECTED.equals(lead.getLeadStatus())).count();
            double conversionRate = leadsCount > 0 ? (double) conversions / leadsCount * 100 : 0;
            double periodRevenue = leadsCount * 10.0;

            String dateRange = formatDateRange(groupStartDate, groupEndDate);

            // Calculate percentage changes from previous group
            double uniqueImpressionsChange = calculatePercentageChange(uniqueImpressions, prevUniqueImpressions);
            double totalImpressionsChange = calculatePercentageChange(periodImpressions, prevTotalImpressions);
            double leadsCountChange = calculatePercentageChange(leadsCount, prevLeadsCount);
            double conversionRateChange = calculatePercentageChange(conversionRate, prevConversionRate);
            double revenueChange = calculatePercentageChange(periodRevenue, prevRevenue);

            // Build and add ReferralData for this group
            timeSeriesData.add(ReferralData.builder()
                    .dateRange(dateRange)
                    .uniqueImpressions(uniqueImpressions)
                    .totalImpressions(periodImpressions)
                    .leadsCount(leadsCount)
                    .conversionRate(conversionRate)
                    .revenue(periodRevenue)
                    .uniqueImpressionsChange(uniqueImpressionsChange)
                    .totalImpressionsChange(totalImpressionsChange)
                    .leadsCountChange(leadsCountChange)
                    .conversionRateChange(conversionRateChange)
                    .revenueChange(revenueChange)
                    .referralCode(referralCode)
                    .organizationId(organizationId)
                    .build());

            // Update previous group metrics
            prevUniqueImpressions = uniqueImpressions;
            prevTotalImpressions = periodImpressions;
            prevLeadsCount = leadsCount;
            prevConversionRate = conversionRate;
            prevRevenue = periodRevenue;
        }

        return timeSeriesData;
    }

    public static double calculatePercentageChange(double currentValue, double previousValue) {
        if (previousValue == 0) {
            if (currentValue == 0) {
                return 0;
            } else {
                return 100; // Convention for increase from zero
            }
        }
        return ((currentValue - previousValue) / previousValue) * 100;
    }

    private String formatDateRange(LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
        return startDate.equals(endDate) ? startDate.format(formatter) : startDate.format(formatter) + "-" + endDate.format(formatter);
    }


    @Transactional
    public CampaignResponse getCampaignsData(Long organizationId) {
        var campaign = campaignRepository.findByOrganizationId(organizationId);
        return generateCampaignResponse(campaign, organizationId);
    }

    @Transactional
    public CampaignResponse getJoinedCampaignsData(Long organizationId) {
        var campaign = campaignRepository.findJoinedOrganizationId(organizationId);
        return generateCampaignResponse(campaign, organizationId);
    }

    private CampaignResponse generateCampaignResponse(List<CampaignEntity> campaign, Long organizationId) {
        AtomicLong leadsCount = new AtomicLong();
        campaign.forEach(campaignEntity -> {
            var referralCode = campaignEntity.getReferralCode();
            var referralLeadCount = leadsRepository.count(referralCode);
            leadsCount.addAndGet(referralLeadCount);
            campaignEntity.setLeadsCount(referralLeadCount);
            campaignEntity.setImpressionCount(impressionsRepository.count(referralCode));
        });
        var campaignStats = campaignStatsRepository.findByOrganizationId(organizationId);
        var activePartners = (long) organizationCollaborationRepository.findActivePartnerIds(organizationId).size();
        var campaignResponse = CampaignResponse.builder()
                .campaignDetails(campaign)
                .partnerCount(activePartners)
                .leadsCount(leadsCount.get());
        if (campaignStats != null) {
            double leadsDifference = calculatePercentageDifference(campaignStats.getLeadsCount(), leadsCount.get());
            double partnerDifference = calculatePercentageDifference(campaignStats.getPartnerCount(), activePartners);
            if (Math.abs(leadsDifference) > 0.1 || Math.abs(partnerDifference) > 0.1) {
                campaignStats.setLeadsCount(leadsCount.get());
                campaignStats.setPartnerCount(activePartners);
                campaignStatsRepository.save(campaignStats);
            }
        } else {
            CampaignStatsEntity newStats = new CampaignStatsEntity();
            newStats.setOrganizationId(organizationId);
            newStats.setLeadsCount(leadsCount.get());
            newStats.setPartnerCount(activePartners);
            campaignStatsRepository.save(newStats);
        }
        return campaignResponse.build();
    }

    private double calculatePercentageDifference(Long oldValue, Long newValue) {
        if (oldValue == null || oldValue == 0) {
            return 0;
        }
        return ((double) (newValue - oldValue) / oldValue) * 100;
    }

    @Transactional
    public CampaignEntity getCampaignData(String referralCode) {
        return campaignRepository.findByReferralCode(referralCode);
    }

    @Transactional
    public ReferralLinkResponse createCampaign(CampaignUpdateRequest campaignUpdateRequest) {
        var referralLink = generateReferralLink(campaignUpdateRequest.getOrganizationId(), campaignUpdateRequest.getUrlRef());
        CampaignStatus status = CampaignStatus.DRAFT;
        if (campaignUpdateRequest.getStatus() != null) {
            status = campaignUpdateRequest.getStatus();
        }
        CampaignEntity campaignEntity = CampaignEntity.builder()
                .referralCode(referralLink.getReferralCode())
                .urlRef(campaignUpdateRequest.getUrlRef())
                .organizationId(campaignUpdateRequest.getOrganizationId())
                .emailRef(campaignUpdateRequest.getEmailRef())
                .referralLink(referralLink.getReferralLink())
                .partnerOrganizationName(campaignUpdateRequest.getPartnerOrganizationName())
                .partnerId(campaignUpdateRequest.getPartnerId())
                .status(status)
                .programName(campaignUpdateRequest.getProgramName())
                .commission(campaignUpdateRequest.isCommission())
                .commissionPercentage(campaignUpdateRequest.getCommissionPercentage())
                .commissionType(campaignUpdateRequest.getCommissionType())
                .minimumThreshold(campaignUpdateRequest.getMinimumThreshold())
                .description(campaignUpdateRequest.getDescription())
                .build();
        var campaign = campaignRepository.save(campaignEntity);
        if (!campaign.isEmailVerified() || !campaign.isDomainVerified()) {
            String code = generateVerificationToken(16);
            String transaction = generateVerificationToken(10);
            UriComponentsBuilder verificationLinkBuilder = generateVerificationLink(baseUrl, code, transaction);
            if (campaignEntity.getReferralCode() != null) {
                verificationLinkBuilder.queryParam("referralCode", campaignEntity.getReferralCode());
            }
            EmailVerification emailVerification = EmailVerification.builder()
                    .organizationId(campaignEntity.getOrganizationId())
                    .transactionId(transaction)
                    .verificationCode(code)
                    .expiresAt(calculateExpirationTime())
                    .build();
            var name = organizationRepository.findNameById(campaignUpdateRequest.getOrganizationId());
            emailVerificationRepository.save(emailVerification);
            emailService.sendByEmail("tutorial_referral", organizationRepository.findEmailById(campaignUpdateRequest.getOrganizationId()), verificationLinkBuilder.build().toUriString(), name);
        }
        referralLink.setOrganizationId(campaign.getOrganizationId());
        referralLink.setUrlRef(campaign.getUrlRef());
        referralLink.setEmailRef(campaign.getEmailRef());
        referralLink.setStatus(campaign.getStatus());
        referralLink.setPartnerOrganizationName(campaign.getPartnerOrganizationName());
        referralLink.setDomain(campaign.getDomain());
        referralLink.setPartnerId(campaign.getPartnerId());
        referralLink.setEmailVerified(campaign.isEmailVerified());
        referralLink.setDomainVerified(campaign.isDomainVerified());
        referralLink.setProgramName(campaign.getProgramName());
        referralLink.setCommission(campaign.isCommission());
        referralLink.setCommissionPercentage(campaign.getCommissionPercentage());
        referralLink.setMinimumThreshold(campaign.getMinimumThreshold());
        referralLink.setCommissionType(campaign.getCommissionType());
        referralLink.setImpressionCount(campaign.getImpressionCount());
        referralLink.setLeadsCount(campaign.getLeadsCount());
        referralLink.setDescription(campaign.getDescription());
        referralLink.setId(campaign.getId());
        return referralLink;

    }

    private List<Leads> mapToLeads(List<LeadsInfo> leadEmails) {
        Map<LocalDate, List<NameEmail>> res = new HashMap<>();
        leadEmails.forEach(leadsInfo -> {
            LeadsEntity.LeadsStatus leadStatus = leadsInfo.getLeadStatus() != null ? leadsInfo.getLeadStatus() : LeadsEntity.LeadsStatus.NEW;
            res.computeIfAbsent(leadsInfo.getDate(), k -> new ArrayList<>()).add(NameEmail.builder()
                    .email(leadsInfo.getEmail())
                    .name(leadsInfo.getName())
                    .leadsStatus(leadStatus)
                    .build());
        });
        return res.entrySet().stream()
                .map(entry -> new Leads(entry.getKey(), entry.getValue()))
                .toList();

    }

    public CampaignEntity patchById(long id, JsonPatch patch) throws Exception {
        Optional<CampaignEntity> campaignEntity = campaignRepository.findById(id);
        campaignEntity.orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH74, id));
        CampaignEntity patchToCampaign = applyPatchToCampaign(patch, campaignEntity.get());
        return campaignRepository.save(patchToCampaign);

    }

    private CampaignEntity applyPatchToCampaign(JsonPatch patch, CampaignEntity targetCampaign) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(targetCampaign, JsonNode.class));
        return objectMapper.treeToValue(patched, CampaignEntity.class);
    }

    public List<RazorpayPaymentDetailsDto> getPaymentTrackingData(String referralCode) {
        return razorpayTrackingRepository.findAllByReferralCode(referralCode);
    }

    public List<RazorpayPaymentDetailsDto> getPaymentTrackingDataTest(String referralCode) {
        return razorpayTrackingTestRepository.findAllByReferralCode(referralCode);
    }

    @Transactional
    public void invitePartner(InviteCampaignRequest inviteCampaignRequest) {
        if (inviteCampaignRepository.findByPartnerIdAndCampaignId(inviteCampaignRequest.partnerId(), inviteCampaignRequest.campaignId()).isPresent()) {
            throw new SharkdomException(ErrorMessages.SH119, inviteCampaignRequest.partnerId());
        }
        campaignRepository.findById(inviteCampaignRequest.campaignId()).ifPresent(campaign -> {
            campaign.setPartnerId(inviteCampaignRequest.partnerId());
            campaignRepository.save(campaign);
            var name = organizationRepository.findNameById(inviteCampaignRequest.partnerId());
            emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder().templateCode("Share_Referral_Invite").organizationName(name).message(inviteCampaignRequest.message()).organizationIds(List.of(inviteCampaignRequest.partnerId())).build(), null, 1L, 1L);
            organizationCollaborationService.sendCampaignCreatedMessage(campaign.getOrganizationId(), campaign.getPartnerId());
            inviteCampaignRepository.save(InviteCampaignEntity.builder()
                    .campaignId(inviteCampaignRequest.campaignId())
                    .partnerId(inviteCampaignRequest.partnerId()).build()
            );
            var receiverOrganizationName = organizationRepository.findNameById(campaign.getOrganizationId());
            Notification notification = Notification.builder()
                    .subject("Invited to campaign")
                    .body(String.format(" You have been invited to campaign by %s.", receiverOrganizationName))
                    .forWeb(true)
                    .organizationId(inviteCampaignRequest.partnerId())
                    .build();
            webSocketHandler.sendMessageToUser(inviteCampaignRequest.partnerId(), notification);
            notificationServiceNote.create(notification);
        });

    }

    public Page<LeadsStats> getLeadsData(String referralCode, int page, int size) {
        Long organizationId = referralRepository.existByReferralCode(referralCode);
        if (organizationId != 0) {
            Pageable pageable = PageRequest.of(page, size);
            Page<LeadsEntity> leads = leadsRepository.findAllByReferralCodeOrderByCreationTimestampDesc(referralCode, pageable);
            var leadsData = leads.getContent().stream().map(lead -> {
                if (lead.getLeadsStatus() == null) {
                    lead.setLeadsStatus(LeadsEntity.LeadsStatus.NEW);
                }
                return new LeadsStats(lead.getCreationTimestamp(), lead.getName(), lead.getEmail(), lead.getLeadsStatus());
            }).toList();
            return new PageImpl<>(leadsData, pageable, leads.getTotalElements());
        } else {
            return Page.empty();
        }
    }

    public DashboardSummaryResponse getPartners(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH75, organizationId));

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        List<CampaignEntity> orgCampaigns = campaignRepository.findByOrganizationId(organizationId);
        int totalPartners = calculateTotalPartners(organizationId);
        int totalLeads = calculateTotalLeads(orgCampaigns);
        double avgPerformance = calculateAveragePerformance(orgCampaigns);
        int partnerGrowth = calculatePartnerGrowth(totalPartners);
        int leadGrowth = calculateLeadGrowth(totalLeads);
        int performanceGrowth = calculatePerformanceGrowth(avgPerformance);
        response.setSummary(new SummarySection(
                new MetricWithGrowth(totalPartners, partnerGrowth),
                new MetricWithGrowth((int) Math.round(avgPerformance), performanceGrowth),
                new MetricWithGrowth(totalLeads, leadGrowth)
        ));
        List<ActivePartnerDto> activePartners = getActivePartners(organizationId);
        response.setActivePartners(activePartners);

        return response;
    }

    private int calculateTotalPartners(Long organizationId) {
        return organizationCollaborationRepository.findActivePartnerIds(organizationId).size();
    }

    private int calculateTotalLeads(List<CampaignEntity> campaigns) {
        int totalLeads = 0;

        for (CampaignEntity campaign : campaigns) {
            String referralCode = campaign.getReferralCode();
            int leads = leadsRepository.countByReferralCode(referralCode);
            totalLeads += leads;
        }

        return totalLeads;
    }

    private double calculateAveragePerformance(List<CampaignEntity> campaigns) {
        if (campaigns.isEmpty()) {
            return 0;
        }

        double totalPerformance = 0;
        for (CampaignEntity campaign : campaigns) {
            String referralCode = campaign.getReferralCode();
            var leadsInfo = leadsRepository.findAllByReferralCode(referralCode, LocalDate.now().minusMonths(3), LocalDate.now());
            var leadsCount = leadsInfo.size();
            var conversions = leadsInfo.stream().filter(lead -> lead.getLeadStatus() != null && lead.getLeadStatus().equals(LeadsEntity.LeadsStatus.CONNECTED)).count();
            double conversionRate = leadsCount > 0 ? (double) conversions / leadsCount * 100 : 0;

            double performanceScore = conversionRate * 100;

            // Cap at 100
            performanceScore = Math.min(performanceScore, 100);
            totalPerformance += performanceScore;
        }

        return totalPerformance / campaigns.size();
    }

    private int calculatePartnerGrowth(int totalPartners) {
        if (totalPartners == 0) {
            return 0;
        } else {
            return random.nextInt(11) + 5;
        }
    }

    private int calculatePerformanceGrowth(double avgPerformance) {
        if (avgPerformance == 0) {
            return 0;
        } else {
            return random.nextInt(11) + 5;
        }
    }

    private List<ActivePartnerDto> getActivePartners(Long organizationId) {
        List<ActivePartnerDto> activePartners = new ArrayList<>();

        // Get all active collaborations for this organization
        List<OrganizationCollaboration> collaborations = organizationCollaborationRepository.findAllActiveCollaboration(organizationId);

        for (OrganizationCollaboration collab : collaborations) {
            long partnerId;
            if (collab.getReceiverOrganizationId() == organizationId) {
                partnerId = collab.getSenderOrganizationId();
            } else {
                partnerId = collab.getReceiverOrganizationId();
            }

            Organization partnerOrg = organizationRepository.findById(partnerId).orElse(null);
            if (partnerOrg == null) {
                continue;
            }

            // Get campaign for this partnership
            CampaignEntity campaign = campaignRepository.findTopByOrganizationIdAndPartnerId(
                            organizationId, partnerId)
                    .orElse(null);

            if (campaign != null) {
                String referralCode = campaign.getReferralCode();
                int referrals = leadsRepository.countByReferralCode(referralCode);

                // Calculate performance score
                int impressions = impressionsRepository.countByReferralCode(referralCode);
                double performanceScore = calculatePerformanceScore(referrals, impressions);

                // Create active partner DTO
                ActivePartnerDto partnerDto = new ActivePartnerDto(
                        partnerId,
                        partnerOrg.getName(),
                        collab.getStatus(),
                        partnerOrg.getWebsite(),
                        partnerOrg.getPrimaryEmail(),
                        (int) Math.round(performanceScore),
                        referrals,
                        impressions
                );

                activePartners.add(partnerDto);
            }
        }

        return activePartners;
    }

    private double calculatePerformanceScore(int referrals, int impressions) {
        if (impressions == 0) {
            return 0;
        }

        double conversionRate = (double) referrals / impressions;
        double performanceScore = conversionRate * 100;

        // Cap at 100
        return Math.min(performanceScore, 100);
    }

    private int calculateLeadGrowth(int totalLeads) {
        if (totalLeads == 0) {
            return 0;
        } else {
            return random.nextInt(11) + 5;
        }
    }

    public PartnerDetailsResponse getPartnerDetails(Long organizationId, Long partnerId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH75, organizationId));

        Organization partnerOrg = organizationRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH76, partnerId));

        OrganizationCollaboration collaboration = organizationCollaborationRepository
                .findBySenderOrganizationIdOrReceiverOrganizationId(organizationId, partnerId);
        if (collaboration == null) {
            throw new ResourceNotFoundException(ErrorMessages.SH77);

        }
        CampaignEntity campaign = campaignRepository.findTopByOrganizationIdAndPartnerId(organizationId, partnerId)
                .orElse(null);
        PartnerDetailsResponse response = new PartnerDetailsResponse();
        PartnerDetails partnerDetails = new PartnerDetails();

        // Basic partner information
        partnerDetails.setName(partnerOrg.getName());
        partnerDetails.setDescription(partnerOrg.getBriefDescription());
        partnerDetails.setStatus(collaboration.getStatus());
        partnerDetails.setMemberSince(formatDate(collaboration.getCreationTimestamp()));

        PartnerInformation partnerInfo = new PartnerInformation();
        partnerInfo.setIndustry(partnerOrg.getSectorType());
        partnerInfo.setLocation(partnerOrg.getAddress());
        partnerInfo.setWebsite(partnerOrg.getWebsite());
        partnerInfo.setEmail(partnerOrg.getPrimaryEmail());
        partnerDetails.setPartnerInformation(partnerInfo);
        PerformanceOverview performanceOverview = new PerformanceOverview();

        if (campaign != null) {
            String referralCode = campaign.getReferralCode();
            int leads = leadsRepository.countByReferralCode(referralCode);
            int impressions = impressionsRepository.countByReferralCode(referralCode);

            performanceOverview.setTotalLeads(leads);
            performanceOverview.setRevenueGenerated(null);
            performanceOverview.setBusinessFitScore(95);
            performanceOverview.setBusinessFitComment("This partner has an excellent business fit score, indicating strong alignment with your company's goals and values.");
        } else {
            performanceOverview.setTotalLeads(0);
            performanceOverview.setRevenueGenerated(null);
            performanceOverview.setBusinessFitScore(0);
            performanceOverview.setBusinessFitComment("No active campaigns found for this partnership.");
        }

        partnerDetails.setPerformanceOverview(performanceOverview);
        List<ReferralProgram> referralPrograms = new ArrayList<>();

        if (campaign != null) {
            String referralCode = campaign.getReferralCode();
            var leads = leadsRepository.findAllByReferralCode(referralCode, LocalDate.now().minusMonths(12), LocalDate.now());

            ReferralProgram program = new ReferralProgram();
            program.setProgramName(campaign.getProgramName());
            program.setStatus(campaign.getStatus());
            program.setJoinedDate(formatDate(campaign.getCreationTimestamp()));
            program.setRating(4.5);
            program.setTopComment("Excellent communication and high-quality leads");

            ReferralMetrics metrics = new ReferralMetrics();
            var leadsCount = leads.size();
            var conversions = leads.stream().filter(lead -> lead.getLeadStatus().equals(LeadsEntity.LeadsStatus.CONNECTED)).count();
            double conversionRate = leadsCount > 0 ? (double) conversions / leadsCount * 100 : 0;
            metrics.setLeads(leads.size());
            metrics.setConversions(conversions);
            metrics.setConversionRate(Math.round(conversionRate * 10) / 10.0);

            program.setMetrics(metrics);
            referralPrograms.add(program);
        }

        partnerDetails.setReferralPrograms(referralPrograms);
        response.setPartner(partnerDetails);

        return response;
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy");
        return formatter.format(date);
    }


    public ReferralScriptCheckResponse testedCampaign(String referralCode, String website) {
        try {
            Document doc = Jsoup.connect(website).get();
            String html = doc.html();

            // Check for script patterns
            boolean containsApiCall = html.contains("https://dev.sharkdom.com/api/referral");
            boolean containsExecuteImpressionFunction = html.contains("function executeImpressionApi()");
            boolean containsSubmitFormFunction = html.contains("function submitForm(");
            boolean containsReferralCode = html.contains(referralCode);

            boolean scriptPresent = containsApiCall && containsExecuteImpressionFunction &&
                    containsSubmitFormFunction && containsReferralCode;

            return new ReferralScriptCheckResponse(containsApiCall, containsExecuteImpressionFunction,
                    containsSubmitFormFunction, containsReferralCode, scriptPresent, null);
        } catch (IOException e) {
            return new ReferralScriptCheckResponse(false, false,
                    false, false, false, "Error: " + e.getMessage());
        }
    }


    public void sendPartnerSpaceMessage(Long senderId, Long receiverId, String message) {
        var collaboration = organizationCollaborationRepository.findBySenderOrganizationIdOrReceiverOrganizationId(senderId, receiverId);
        if (Objects.nonNull(collaboration)) {
            SendMessageRequest sendMessageRequest = new SendMessageRequest(
                    collaboration.getId(),
                    message,
                    senderId,
                    LinkerType.EMPTY,
                    Flag.ASSISTANT,
                    senderId,
                    receiverId
            );
            organizationCollaborationService.sendMessage(sendMessageRequest);
        }
    }
}
