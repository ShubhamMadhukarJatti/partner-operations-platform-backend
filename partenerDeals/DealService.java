package com.sharkdom.service.partenerDeals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.constants.partnerDeals.DealStage;
import com.sharkdom.constants.partnerDeals.DealStatus;
import com.sharkdom.dto.DealResponseDto;
import com.sharkdom.emailOutreach.entity.Email;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organization.OrganizationUserMapping;
import com.sharkdom.entity.partenearDeals.Deal;
import com.sharkdom.entity.user.User;
import com.sharkdom.model.partnerDeals.DealCheckResponse;
import com.sharkdom.model.partnerDeals.DealRequestDto;
import com.sharkdom.offlinePartner.entity.OfflinePartnerInvite;
import com.sharkdom.offlinePartner.model.OfflinePartnerInviteRequest;
import com.sharkdom.offlinePartner.model.PartnerInviteStatus;
import com.sharkdom.offlinePartner.repository.OfflinePartnerInviteRepository;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationCollaborationRepository;
import com.sharkdom.repository.partnerDeals.DealRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.salesforce.dto.SalesforceTokenResponse;
import com.sharkdom.salesforce.service.SalesforceAuthService;
import com.sharkdom.salesforce.service.SalesforceService;
import com.sharkdom.salesforce.service.SalesforceSyncService;
import com.sharkdom.service.ai.ZohoService;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.partenerDeals.hubspot.HubSpotAuthService;
import com.sharkdom.service.partenerDeals.hubspot.HubSpotSyncService;
import com.sharkdom.service.partenerDeals.hubspot.dto.CreateDealRequest;
import com.sharkdom.service.partenerDeals.hubspot.dto.DealStatusCountDto;
import com.sharkdom.service.partenerDeals.hubspot.dto.TokenResponse;
import com.sharkdom.util.Util;
import com.sharkdom.zoho.service.ZohoAuthService;
import com.sharkdom.zoho.service.ZohoDealSyncService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.sharkdom.service.organization.OrganizationService.ALGORITHM;

@Service
@Slf4j
public class  DealService {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();
    @Autowired
    private DealRepository dealRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrganizationCollaborationRepository organizationCollaborationRepository;
    @Autowired
    private IntegrationRepository integrationRepository;
    @Autowired
    private HubSpotAuthService hubSpotAuthService;
    @Autowired
    private HubSpotSyncService hubSpotDealService;
    @Autowired
    private OrganizationUserMappingRepository organizationUserMappingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SalesforceService salesforceService;
    @Autowired
    private SalesforceAuthService salesforceAuthService;
    @Autowired
    private SalesforceSyncService salesforceSyncService;
    @Autowired
    private ZohoDealSyncService zohoDealSyncService;
    @Autowired
    private ZohoService zohoService;

    @Value("${env}")
    private String env;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ZohoAuthService zohoAuthService;
    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OfflinePartnerInviteRepository offlinePartnerInviteRepository;

    public List<Deal> getAndUpdateExpiredDealsByOrgId(Long targetOrgId) {
        Long currentOrgId = Util.getOrgIdFromToken();
        if(organizationCollaborationRepository.findActivePartnerIds(currentOrgId).isEmpty()){
            throw  new EntityNotFoundException("Entities are not partners");
        }
        List<Deal> deals = dealRepository.findByDealerOrgIdOrVendorOrgId(currentOrgId, targetOrgId);
        getAndUpdateExpiration(deals);
        return deals;
    }

    private List<Deal> getAndUpdateExpiration(List<Deal> deals) {
        List<Deal> updatedDeals = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Deal deal : deals) {
            if (deal.getDealStage() != DealStage.EXPIRED && deal.getExpectedClosingTime() != null && deal.getCreationTimestamp() != null) {
                LocalDate creationDate = deal.getCreationTimestamp().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate expectedCloseDate = creationDate.plusDays(deal.getExpectedClosingTime());

                if (expectedCloseDate.isBefore(today)) {
                    deal.setDealStage(DealStage.EXPIRED);
                    updatedDeals.add(deal);
                }
            }
        }

        if (!updatedDeals.isEmpty()) {
            dealRepository.saveAll(updatedDeals);
        }
        return updatedDeals;
    }

//    public List<Deal> getAllRelatedDeals(DealStage dealStage) {
//        log.info("Fetching all related deals for organization ");
//        List<Deal> sentDeals = getCreateDeals(dealStage);
//        List<Deal> receivedDeals = getReceivedDeals(dealStage);
//        String orgName=null;
//        for(Deal deal : sentDeals){
//            log.info("Sent Deal ID: {}, Stage: {}", deal.getDealId(), deal.getDealStage());
//            Long vendorOrgId = deal.getVendorOrgId();
//            Optional<Organization> organization = organizationRepository.findById(vendorOrgId);
//            Organization org = organization.get();
//            orgName = org.getName();
//        }
//        List<Deal> allDeals = new ArrayList<>();
//        allDeals.addAll(sentDeals);
//        allDeals.addAll(receivedDeals);
//        log.info("Total deals fetched: {}", allDeals.size());
//        return allDeals;
//    }

    public List<Deal> getCreateDeals(DealStatus dealStage){
        getAndUpdateExpiration(dealRepository.findByDealerOrgId(Util.getOrgIdFromToken()));
        return dealRepository.findByDealerOrgIdAndDealStatus(Util.getOrgIdFromToken(),dealStage);
    }


    public List<Deal> getReceivedDeals(DealStatus dealStage){
        getAndUpdateExpiration(dealRepository.findByVendorOrgId(Util.getOrgIdFromToken()));
        return dealRepository.findByVendorOrgIdAndDealStatus(Util.getOrgIdFromToken(),dealStage);
    }

    @Transactional
    public Deal updateApprovalStatus(String dealId, Boolean isApproved, Long dealProtectionPeriod,IntegrationType integrationType) throws JsonProcessingException {
        if (dealId == null || dealId.isBlank()) {
            throw new IllegalArgumentException("Deal ID must not be null or empty.");
        }
        if (isApproved == null) {
            throw new IllegalArgumentException("Approval status must not be null.");
        }

        log.info("Updating approval status for Deal ID: {} to {}", dealId, isApproved);

        Deal deal = dealRepository.findByDealId(dealId)
                .orElseThrow(() -> new EntityNotFoundException("Deal not found with ID: " + dealId));

        deal.setIsApproved(isApproved);
        deal.setProvider(integrationType.name());
        deal.setDealProtectionPeriod(dealProtectionPeriod);
        deal.setDealStage(isApproved ? DealStage.APPROVED : DealStage.WAITING_FOR_APPROVAL);
        deal.setDealStatus(isApproved ? DealStatus.ACTIVE : DealStatus.REJECTED);

        Deal updatedDeal = dealRepository.save(deal);
        log.info("Deal updated in database: {}", updatedDeal.getDealId());
        
        // Sync to HubSpot if approved
        if (isApproved) {
            if (integrationType == IntegrationType.SALESFORCE) {
                // Fetch refresh token
                IntegrationDetails integrationDetails = integrationRepository
                        .findByOrganizationIdAndIntegrationType(updatedDeal.getVendorOrgId(), IntegrationType.SALESFORCE);
                SalesforceTokenResponse salesforceTokenResponse = salesforceAuthService.refreshAccessToken(integrationDetails.getRefreshToken());
                String accessToken = salesforceTokenResponse.accessToken();
                String instanceUrl = salesforceTokenResponse.instanceUrl();
                log.info("Access Token for Salesforce: {}", accessToken);
                String newDealIdFromSalesforce = salesforceSyncService.createDeal(instanceUrl, accessToken, updatedDeal);
                log.info("Deal created on Salesforce for Deal ID: {}", updatedDeal.getDealId());
                updatedDeal.setSalesforceDealId(newDealIdFromSalesforce);
                Deal save = dealRepository.save(updatedDeal);
                log.info("Deal updated in database after Salesforce creation: {}", save.getSalesforceDealId());
            }

            if (integrationType == IntegrationType.ZOHO) {
                // Fetch refresh token
                IntegrationDetails integrationDetails = integrationRepository
                        .findByOrganizationIdAndIntegrationType(updatedDeal.getVendorOrgId(), IntegrationType.ZOHO);
                String accessToken = zohoAuthService.refreshAccessToken(integrationDetails.getRefreshToken());
                String zohoDealSyncServiceDealId = zohoDealSyncService.createDeal(accessToken, updatedDeal);
                log.info("Deal created on Zoho for Deal ID: {}",zohoDealSyncServiceDealId );
                updatedDeal.setZohoDealId(zohoDealSyncServiceDealId);
                Deal save = dealRepository.save(updatedDeal);
                log.info("Deal updated in database after Zoho creation: {}", save.getZohoDealId());
            }

            if (integrationType == IntegrationType.HUBSPOT) {
                // Fetch refresh token
                IntegrationDetails integrationDetails = integrationRepository
                        .findByOrganizationIdAndIntegrationType(updatedDeal.getVendorOrgId(), IntegrationType.HUBSPOT);

                String refreshToken = integrationDetails.getRefreshToken();
                log.info("Using refresh token for HubSpot: {}", refreshToken);
                // Step 1: Get Access Token
                TokenResponse accessTokenUsingRefreshToken = hubSpotAuthService.getAccessTokenUsingRefreshToken(refreshToken);
                String accessToken= accessTokenUsingRefreshToken.getAccessToken();
                log.info("Access Token for HubSpot: {}", accessToken);

                // Step 2: Prepare Deal Payload
                CreateDealRequest hubspotDeal = new CreateDealRequest();
                Map<String, String> properties = new HashMap<>();
                properties.put("dealname", updatedDeal.getCustomerAccountName());
                properties.put("dealstage", "appointmentscheduled");
                properties.put("pipeline", "default");
                properties.put("amount", "0");
                properties.put("dealtype", "newbusiness"); // deal with a new customer, exiting business, etc.

                hubspotDeal.setProperties(properties);

                // Step 3: Create Deal on HubSpot
                try {
                    String hubspotResponse = hubSpotDealService.createDeal(hubspotDeal, accessToken);
                    log.info("Deal created on HubSpot: {}", hubspotResponse);
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode root = objectMapper.readTree(hubspotResponse);
                    String hubspotDealId = root.path("id").asText();
                    log.info("HubSpot Deal ID: {}", hubspotDealId);
                    // Update the deal with HubSpot ID
                    updatedDeal.setHotspotDealId(hubspotDealId);
                    Deal updatedAfterDealCreation = dealRepository.save(updatedDeal);
                    log.info("Deal updated in database after HubSpot creation: {}", updatedAfterDealCreation.getDealId());
                } catch (Exception e) {
                    log.error("Failed to create deal on HubSpot", e);
                    // Optional: throw exception or continue
                }
            }
        }
        return updatedDeal;
    }


    @Transactional
    public Deal updateApprovalStatusExternalPartnerPortalToInternal(String dealId, Boolean isApproved, Long dealProtectionPeriod,IntegrationType integrationType) throws JsonProcessingException {
        if (dealId == null || dealId.isBlank()) {
            throw new IllegalArgumentException("Deal ID must not be null or empty.");
        }
        if (isApproved == null) {
            throw new IllegalArgumentException("Approval status must not be null.");
        }

        log.info("Updating approval status for Deal ID: {} to {}", dealId, isApproved);

        Deal deal = dealRepository.findByDealId(dealId)
                .orElseThrow(() -> new EntityNotFoundException("Deal not found with ID: " + dealId));

        deal.setIsApproved(isApproved);
        deal.setProvider(integrationType.name());
        deal.setDealProtectionPeriod(dealProtectionPeriod);
        deal.setDealStage(isApproved ? DealStage.APPROVED : DealStage.WAITING_FOR_APPROVAL);
        deal.setDealStatus(isApproved ? DealStatus.ACTIVE : DealStatus.REJECTED);

        Deal updatedDeal = dealRepository.save(deal);
        return updatedDeal;
    }


    @Transactional
    public Deal updateApprovalStatusInternalPartnerPortalToExternal(String dealId, Boolean isApproved, Long dealProtectionPeriod,IntegrationType integrationType) throws JsonProcessingException {
        if (dealId == null || dealId.isBlank()) {
            throw new IllegalArgumentException("Deal ID must not be null or empty.");
        }
        if (isApproved == null) {
            throw new IllegalArgumentException("Approval status must not be null.");
        }

        log.info("Updating approval status for Deal ID: {} to {}", dealId, isApproved);

        Deal deal = dealRepository.findByDealId(dealId)
                .orElseThrow(() -> new EntityNotFoundException("Deal not found with ID: " + dealId));

        deal.setIsApproved(isApproved);
        deal.setProvider(integrationType.name());
        deal.setDealProtectionPeriod(dealProtectionPeriod);
        deal.setDealStage(isApproved ? DealStage.APPROVED : DealStage.WAITING_FOR_APPROVAL);
        deal.setDealStatus(isApproved ? DealStatus.ACTIVE : DealStatus.REJECTED);

        Deal updatedDeal = dealRepository.save(deal);
        return updatedDeal;
    }


    public List<Map<String, String>> invitePartnersExternalPartnerPortal(OfflinePartnerInviteRequest offlinePartnerInviteRequest) {
        List<OfflinePartnerInvite> invites;
        if (offlinePartnerInviteRequest.sendAll()) {
            invites = offlinePartnerInviteRepository.findByOrganizationId(offlinePartnerInviteRequest.organizationId());
        } else {
            invites = offlinePartnerInviteRepository.findByOrganizationIdAndEmailIn(offlinePartnerInviteRequest.organizationId(), offlinePartnerInviteRequest.emails());
        }
        return invites.stream().map(partnerInvite -> {
            String userId = RandomStringUtils.random(10, true, true);
            String encode = userId + ":" + offlinePartnerInviteRequest.organizationId() + ":" + "role" + ":" + partnerInvite.getEmail();
            var encodedValue = encrypt(encode);
            String url;
            if (env.equalsIgnoreCase("dev")) {
                url = "https://dev.sharkdom.com/register?utm_register=" + encodedValue;
            } else {
                url = "https://sharkdom.com/register?utm_register=" + encodedValue;
            }
            emailService.invitePartner("Partner_invite", partnerInvite.getEmail(), url, "", offlinePartnerInviteRequest.organizationId());
            partnerInvite.setStatus(PartnerInviteStatus.INVITE_SENT.name());
            offlinePartnerInviteRepository.save(partnerInvite);
            return Map.of("signupUrl", url);
        }).collect(Collectors.toList());

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
            throw new RuntimeException(e);
        }
    }


    public Deal updateDealFields(Long dealId, DealRequestDto dto) {
        Deal existingDeal = dealRepository.findById(dealId)
                .orElseThrow(() -> new EntityNotFoundException("Deal not found with ID: " + dealId));

        if (dto.getCustomerAccountName() != null) existingDeal.setCustomerAccountName(dto.getCustomerAccountName());
        if (dto.getWebsite() != null) existingDeal.setWebsite(dto.getWebsite());
        if (dto.getHeadQuarterLocation() != null) existingDeal.setHeadQuarterLocation(dto.getHeadQuarterLocation());
        if (dto.getEstimatedAcv() != null) existingDeal.setEstimatedAcv(dto.getEstimatedAcv());
        if (dto.getExpectedClosingTime() != null) existingDeal.setExpectedClosingTime(dto.getExpectedClosingTime());
        if (dto.getCurrentSolution() != null) existingDeal.setCurrentSolution(dto.getCurrentSolution());
        if (dto.getRequirements() != null) existingDeal.setRequirements(dto.getRequirements());
        if (dto.getDealProtectionPeriod() != null) existingDeal.setDealProtectionPeriod(dto.getDealProtectionPeriod());
        if (dto.getCustomFields() != null) {
            try {
                String json = objectMapper.writeValueAsString(dto.getCustomFields());
                existingDeal.setCustomFields(json);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        if (dto.getDealStage() != null) existingDeal.setDealStage(dto.getDealStage());
        if (dto.getSource() != null) existingDeal.setSource(dto.getSource());
        if (dto.getIsApproved() != null) existingDeal.setIsApproved(dto.getIsApproved());
        if (dto.getDealerOrgId() != null) existingDeal.setDealerOrgId(dto.getDealerOrgId());
        if (dto.getVendorOrgId() != null) existingDeal.setVendorOrgId(dto.getVendorOrgId());

        return dealRepository.save(existingDeal);
    }

    public Deal createDeal(DealRequestDto dto) {
        String userFromToken = Util.getUserFromToken();
        log.info("Creating deal for user: {}", userFromToken);
        Deal deal = dto.toEntity();
        deal.setDealStage(DealStage.WAITING_FOR_APPROVAL); // Optional: override if not provided
        deal.setIsApproved(false); // Optional: override if not provided
        deal.setDealerOrgId(Util.getOrgIdFromToken());
        deal.setDealId(String.valueOf(UUID.randomUUID()));
        deal.setDealCode(generateDealCode());
        deal.setDealStatus(DealStatus.PENDING);
        deal.setIsSent(true);
        Deal savedDeal = dealRepository.save(deal);
        Optional<User> user = userRepository.findByEmail(userFromToken);
        String name = user.get().getName();
        log.info("User Name: {}", name);
        savedDeal.setPointOfContact(name);
        Deal save = dealRepository.save(savedDeal);
        return savedDeal;
    }

    public Deal createDealExternalPartnerPortal(DealRequestDto dto) {
        Deal deal = dto.toEntity();
        deal.setDealStage(DealStage.WAITING_FOR_APPROVAL); // Optional: override if not provided
        deal.setIsApproved(false);// Optional: override if not provided
        deal.setExternalPartnerCode(dto.getExternalPartnerCode());
        deal.setIsExternalPartnerPortalDeal(true);
        deal.setDealerOrgId(0l);
        deal.setDealId(String.valueOf(UUID.randomUUID()));
        deal.setDealCode(generateDealCode());
        deal.setDealStatus(DealStatus.PENDING);
        deal.setIsSent(true);
        deal.setUserId(dto.getUserId());
        Deal savedDeal = dealRepository.save(deal);
        return savedDeal;
    }

    public Deal createDealInternalToExternalPartnerPortal(DealRequestDto dto) {
        var orgId = Util.getOrgIdFromToken();
        Deal deal = dto.toEntity();
        deal.setDealStage(DealStage.WAITING_FOR_APPROVAL);  // Optional: override if not provided
        deal.setIsApproved(false);                         // Optional: override if not provided
        deal.setExternalPartnerCode(dto.getExternalPartnerCode());
        deal.setIsInternalToExternalPartnerPortalDeal(true);
        deal.setDealerOrgId(orgId);
        deal.setDealId(String.valueOf(UUID.randomUUID()));
        deal.setDealCode(generateDealCode());
        deal.setDealStatus(DealStatus.PENDING);
        deal.setIsSent(true);
        Deal savedDeal = dealRepository.save(deal);
        return savedDeal;
    }

    public static String generateDealCode() {
        String randomPart = randomAlphaNumeric();
        log.info("DealCode"+ "DL" + randomPart);
        return "DL" + randomPart;
    }

    private static String randomAlphaNumeric() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHANUM.charAt(random.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }

    public DealCheckResponse checkDealExistence(Long vendorOrgId, String website) {
        DealCheckResponse response = new DealCheckResponse();

        if (vendorOrgId == null && (website == null || website.isBlank())) {
            response.setMessage("vendorOrgId and website must not be null or empty");
            return response;
        }

        if (vendorOrgId == null) {
            response.setMessage("vendorOrgId must not be null");
            return response;
        }

        if (website == null || website.isBlank()) {
            response.setMessage("website must not be null or empty");
            return response;
        }

        if (dealRepository.findByVendorOrgIdAndWebsite(vendorOrgId, website).isPresent()) {
            response.setMessage("Deal already exists with same vendor");
            response.setAlreadyExistsWithSameVendor(true);
        } else if (dealRepository.findByWebsite(website).isPresent()) {
            response.setMessage("Deal already exists with another vendor");
            response.setAlreadyExistsWithAnotherVendor(true);
        } else {
            response.setMessage("No existing deal found");
        }

        return response;
    }

    public DealCheckResponse checkDealExistenceForEPS(String externalPartnerCode, String website) {
        DealCheckResponse response = new DealCheckResponse();

        if (externalPartnerCode == null && (website == null || website.isBlank())) {
            response.setMessage("external partner code and website must not be null or empty");
            return response;
        }

        if (externalPartnerCode == null) {
            response.setMessage("external partner code must not be null");
            return response;
        }

        if (website == null || website.isBlank()) {
            response.setMessage("website must not be null or empty");
            return response;
        }

        if (dealRepository.findByExternalPartnerCodeAndWebsite(externalPartnerCode, website).isPresent()) {
            response.setMessage("Deal already exists with same vendor");
            response.setAlreadyExistsWithSameVendor(true);
        } else if (dealRepository.findByWebsite(website).isPresent()) {
            response.setMessage("Deal already exists with another vendor");
            response.setAlreadyExistsWithAnotherVendor(true);
        } else {
            response.setMessage("No existing deal found");
        }

        return response;
    }

    public List<DealStatusCountDto> getDealCountsByStatusForOrg(long organizationId) {
        log.info("Fetching deal counts by status for organization");
        Map<DealStatus, Long> statusCountMap = new EnumMap<>(DealStatus.class);

        // Initialize all to 0
        for (DealStatus status : DealStatus.values()) {
            statusCountMap.put(status, 0L);
        }

        // Get counts from DB for that org
        List<Object[]> rawCounts = dealRepository.getDealCountsByStatusForOrg(organizationId);
        for (Object[] row : rawCounts) {
            DealStatus status = (DealStatus) row[0];
            Long count = (Long) row[1];
            statusCountMap.put(status, count);
        }

        // Prepare ordered counts
        List<DealStatusCountDto> orderedCounts = new ArrayList<>();
        orderedCounts.add(new DealStatusCountDto("ACTIVE", statusCountMap.get(DealStatus.ACTIVE)));
        orderedCounts.add(new DealStatusCountDto("PENDING", statusCountMap.get(DealStatus.PENDING)));
        orderedCounts.add(new DealStatusCountDto("REJECTED", statusCountMap.get(DealStatus.REJECTED)));
        orderedCounts.add(new DealStatusCountDto("EXPIRED", statusCountMap.get(DealStatus.EXPIRED)));

        // Add ALL (total)
        long totalCount = statusCountMap.values().stream().mapToLong(Long::longValue).sum();
        orderedCounts.add(new DealStatusCountDto("ALL", totalCount));

        return orderedCounts;
    }

    public List<DealStatusCountDto> getDealCountsByStatusForUser(String userId,Long orgId) {
        log.info("Fetching deal counts by status for organization");
        Map<DealStatus, Long> statusCountMap = new EnumMap<>(DealStatus.class);

        // Initialize all to 0
        for (DealStatus status : DealStatus.values()) {
            statusCountMap.put(status, 0L);
        }

        // Get counts from DB for that org
        List<Object[]> rawCounts = dealRepository.getDealCountsByStatusForUser(userId,orgId);
        for (Object[] row : rawCounts) {
            DealStatus status = (DealStatus) row[0];
            Long count = (Long) row[1];
            statusCountMap.put(status, count);
        }

        // Prepare ordered counts
        List<DealStatusCountDto> orderedCounts = new ArrayList<>();
        orderedCounts.add(new DealStatusCountDto("ACTIVE", statusCountMap.get(DealStatus.ACTIVE)));
        orderedCounts.add(new DealStatusCountDto("PENDING", statusCountMap.get(DealStatus.PENDING)));
        orderedCounts.add(new DealStatusCountDto("REJECTED", statusCountMap.get(DealStatus.REJECTED)));
        orderedCounts.add(new DealStatusCountDto("EXPIRED", statusCountMap.get(DealStatus.EXPIRED)));

        // Add ALL (total)
        long totalCount = statusCountMap.values().stream().mapToLong(Long::longValue).sum();
        orderedCounts.add(new DealStatusCountDto("ALL", totalCount));

        return orderedCounts;
    }


    public List<DealStatusCountDto> getDealCountByStatusForOrg(long organizationId) {
        log.info("Fetching deal counts by status for organization");
        Map<DealStatus, Long> statusCountMap = new EnumMap<>(DealStatus.class);

        // Initialize all to 0
        for (DealStatus status : DealStatus.values()) {
            statusCountMap.put(status, 0L);
        }

        // Get counts from DB for that org
        List<Object[]> rawCounts = dealRepository.getDealCountsByStatusForOrgInclusive(organizationId);
        for (Object[] row : rawCounts) {
            DealStatus status = (DealStatus) row[0];
            Long count = (Long) row[1];
            statusCountMap.put(status, count);
        }

        // Prepare ordered counts
        List<DealStatusCountDto> orderedCounts = new ArrayList<>();
        orderedCounts.add(new DealStatusCountDto("ACTIVE", statusCountMap.get(DealStatus.ACTIVE)));
        orderedCounts.add(new DealStatusCountDto("PENDING", statusCountMap.get(DealStatus.PENDING)));
        orderedCounts.add(new DealStatusCountDto("REJECTED", statusCountMap.get(DealStatus.REJECTED)));
        orderedCounts.add(new DealStatusCountDto("EXPIRED", statusCountMap.get(DealStatus.EXPIRED)));

        // Add ALL (total)
        long totalCount = statusCountMap.values().stream().mapToLong(Long::longValue).sum();
        orderedCounts.add(new DealStatusCountDto("ALL", totalCount));

        return orderedCounts;
    }

    public Page<Deal> getDealsByUserAndStatus(String userId, DealStatus dealStatus, Long vendorOrgId,int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return dealRepository.findByUserIdAndDealStatusAndVendorOrgId(userId, dealStatus, vendorOrgId,pageable);
    }

    public Page<Deal> getDealsInExternalPartnerPortalSend(String externalPartnerCode, DealStatus dealStatus, Long vendorOrgId,int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return dealRepository.findByExternalPartnerCodeAndDealStatusAndVendorOrgIdAndIsExternalPartnerPortalDeal(externalPartnerCode, dealStatus, vendorOrgId,true,pageable);
    }

    public Page<Deal> getDealsInExternalPartnerPortalReceived(String externalPartnerCode, Long vendorOrgId,int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return dealRepository.findByExternalPartnerCodeAndVendorOrgIdAndIsInternalToExternalPartnerPortalDeal(externalPartnerCode, vendorOrgId,true,pageable);
    }

    public Page<Deal> getDealsInInternalPartnerReceived(String externalPartnerCode,int page, int size) {
        var orgIdFromToken = Util.getOrgIdFromToken();
        Pageable pageable = PageRequest.of(page, size);
        return dealRepository.findByExternalPartnerCodeAndDealerOrgIdAndIsInternalToExternalPartnerPortalDeal(externalPartnerCode, orgIdFromToken,false,pageable);
    }

    public Page<Deal> getDealsInInternalPartnerSend(String externalPartnerCode,int page, int size) {
        var orgIdFromToken = Util.getOrgIdFromToken();
        Pageable pageable = PageRequest.of(page, size);
        return dealRepository.findByExternalPartnerCodeAndDealerOrgIdAndIsInternalToExternalPartnerPortalDeal(externalPartnerCode, orgIdFromToken,true,pageable);
    }



    public JSONObject getAllRelatedDeals(DealStatus dealStage)throws JSONException {
        log.info("Fetching all related deals for organization");
        Long orgIdFromToken = Util.getOrgIdFromToken();
        JSONObject response = new JSONObject();

        List<Deal> sentDeals = getCreateDeals(dealStage);
        List<Deal> receivedDeals = getReceivedDeals(dealStage);

        // 1. Collect all unique orgIds
        Set<Long> orgIds = new HashSet<>();
        sentDeals.forEach(deal -> orgIds.add(deal.getVendorOrgId()));
        receivedDeals.forEach(deal -> orgIds.add(deal.getDealerOrgId()));

        // 2. Fetch organizations once
        Map<Long, String> orgIdToName = organizationRepository.findAllById(orgIds)
                .stream()
                .collect(Collectors.toMap(
                        org -> org.getId(),
                        org -> org.getName() != null ? org.getName() : "Unknown"
                ));

        // --- SENT DEALS GROUPED BY ORG ---
        Map<String, List<DealResponseDto>> sentGrouped = sentDeals.stream()
                .map(this::mapToDto)
                .peek(dto -> dto.setOrgName(orgIdToName.getOrDefault(dto.getVendorOrgId(), "Unknown")))
                .collect(Collectors.groupingBy(DealResponseDto::getOrgName));

        JSONArray sentDealsArray = new JSONArray();
        sentGrouped.forEach((orgName, dealsList) -> {
            JSONObject orgDealsObj = new JSONObject();
            try {
                orgDealsObj.put("orgName", orgName);
                orgDealsObj.put("deals", dealsList);
                orgDealsObj.put("deal_counts",dealsList.size());
                double totalDealSize = dealsList.stream()
                        .mapToDouble(dto -> {
                            try {
                                return dto.getDealSize() != null && !dto.getDealSize().isEmpty()
                                        ? Double.parseDouble(dto.getDealSize())
                                        : 0.0;
                            } catch (NumberFormatException e) {
                                return 0.0;
                            }
                        })
                        .sum();
                orgDealsObj.put("totalDealSize", totalDealSize);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            sentDealsArray.put(orgDealsObj);
        });

        // --- RECEIVED DEALS GROUPED BY CURRENT ORG NAME ---
        Map<String, List<DealResponseDto>> receivedGrouped = receivedDeals.stream()
                .map(this::mapToDto)
                .peek(dto -> dto.setOrgName(orgIdToName.getOrDefault(dto.getDealerOrgId(), "Unknown")))
                .collect(Collectors.groupingBy(DealResponseDto::getOrgName));

        JSONArray receivedDealsArray = new JSONArray();
        receivedGrouped.forEach((orgName, dealsList) -> {
            JSONObject orgDealsObj = new JSONObject();
            try {
                orgDealsObj.put("orgName", orgName);
                orgDealsObj.put("deals", dealsList);
                orgDealsObj.put("deal_counts", dealsList.size());
                double totalDealSize = dealsList.stream()
                        .mapToDouble(dto -> {
                            try {
                                return dto.getDealSize() != null && !dto.getDealSize().isEmpty()
                                        ? Double.parseDouble(dto.getDealSize())
                                        : 0.0;
                            } catch (NumberFormatException e) {
                                return 0.0;
                            }
                        })
                        .sum();
                orgDealsObj.put("totalDealSize", totalDealSize);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            receivedDealsArray.put(orgDealsObj);
        });

        response.put("sentDeals", sentDealsArray);
        response.put("receivedDeals", receivedDealsArray);
        response.put("deals_counts",getDealCountByStatusForOrg(orgIdFromToken));
        log.info("Sent groups: {}, Received deals: {}", sentDealsArray.length(), receivedDealsArray.length());
        return response;
    }

    private DealResponseDto mapToDto(Deal deal) {
        DealResponseDto dto = new DealResponseDto();
        dto.setCustomerAccountName(
                deal.getCustomerAccountName() != null ? deal.getCustomerAccountName() : "Unknown");
        dto.setDealId(
                deal.getDealId() != null ? deal.getDealId() : "N/A");
        dto.setDealCode(
                deal.getDealCode() != null ? deal.getDealCode() : "N/A");
        dto.setWebsite(
                deal.getWebsite() != null ? deal.getWebsite() : "");
        dto.setHeadQuarterLocation(
                deal.getHeadQuarterLocation() != null ? deal.getHeadQuarterLocation() : "");
        dto.setEstimatedAcv(
                deal.getEstimatedAcv() != null ? deal.getEstimatedAcv() : 0);
        dto.setExpectedClosingTime(
                deal.getExpectedClosingTime() != null ? deal.getExpectedClosingTime() : 0);
        dto.setCurrentSolution(
                deal.getCurrentSolution() != null ? deal.getCurrentSolution() : "");
        dto.setRequirements(
                deal.getRequirements() != null ? deal.getRequirements() : "");
        dto.setCustomFields(
                deal.getCustomFields() != null ? deal.getCustomFields() : "{}");
        dto.setCustomFieldsMap(
                deal.getCustomFieldsMap() != null ? new HashMap<>(deal.getCustomFieldsMap()) : new HashMap<>());
        dto.setDealStage(
                deal.getDealStage() != null ? deal.getDealStage().name() : "UNKNOWN");
        dto.setSource(
                deal.getSource() != null ? deal.getSource().name() : "UNKNOWN");
        dto.setIsApproved(
                deal.getIsApproved() != null ? deal.getIsApproved() : false);
        dto.setDealerOrgId(
                deal.getDealerOrgId() != null ? deal.getDealerOrgId() : 0L);
        dto.setVendorOrgId(
                deal.getVendorOrgId() != null ? deal.getVendorOrgId() : 0L);
        dto.setDealProtectionPeriod(
                deal.getDealProtectionPeriod() != null ? deal.getDealProtectionPeriod() : 0L);
        dto.setIsSent(
                deal.getIsSent() != null ? deal.getIsSent() : false);
        dto.setDealStatus(
                deal.getDealStatus() != null ? deal.getDealStatus().name() : "UNKNOWN");
        dto.setDealSize(
                deal.getDealSize() != null ? deal.getDealSize() : "");
        dto.setHotspotDealId(
                deal.getHotspotDealId() != null ? deal.getHotspotDealId() : "");
        dto.setLastUpdatedTimestamp(
                deal.getLastUpdatedTimestamp() != null ? deal.getLastUpdatedTimestamp() : null);
        dto.setLastActivity(
                deal.getLastActivity() != null ? deal.getLastActivity() : "");
        dto.setPointOfContact(
                deal.getPointOfContact() != null ? deal.getPointOfContact() : "");
        dto.setSalesforceDealId(
                deal.getSalesforceDealId() != null ? deal.getSalesforceDealId() : "");
        dto.setZohoDealId(
                deal.getZohoDealId() != null ? deal.getZohoDealId() : "");
        dto.setProvider(deal.getProvider() != null ? deal.getProvider() : "");
        dto.setOrgName("Unknown"); // default, will be overwritten when fetching org
        return dto;
    }

    public List<DealStatusCountDto> getDealCountsByExternalPartnerCode(String externalPartnerCode,Long orgId) {
        log.info("Fetching deal counts by status for organization");
        Map<DealStatus, Long> statusCountMap = new EnumMap<>(DealStatus.class);

        // Initialize all to 0
        for (DealStatus status : DealStatus.values()) {
            statusCountMap.put(status, 0L);
        }

        // Get counts from DB for that org
        List<Object[]> rawCounts = dealRepository.getDealCountsByStatusForExternalPartner(externalPartnerCode, orgId);
        for (Object[] row : rawCounts) {
            DealStatus status = (DealStatus) row[0];
            Long count = (Long) row[1];
            statusCountMap.put(status, count);
        }

        // Prepare ordered counts
        List<DealStatusCountDto> orderedCounts = new ArrayList<>();
        orderedCounts.add(new DealStatusCountDto("ACTIVE", statusCountMap.get(DealStatus.ACTIVE)));
        orderedCounts.add(new DealStatusCountDto("PENDING", statusCountMap.get(DealStatus.PENDING)));
        orderedCounts.add(new DealStatusCountDto("REJECTED", statusCountMap.get(DealStatus.REJECTED)));
        orderedCounts.add(new DealStatusCountDto("EXPIRED", statusCountMap.get(DealStatus.EXPIRED)));

        // Add ALL (total)
        long totalCount = statusCountMap.values().stream().mapToLong(Long::longValue).sum();
        orderedCounts.add(new DealStatusCountDto("ALL", totalCount));

        return orderedCounts;
    }


}

