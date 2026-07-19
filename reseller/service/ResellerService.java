package com.sharkdom.reseller.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.entity.catalogue.PartnerTier;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.repository.catalogue.PartnerTierRepository;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.reseller.dto.*;
import com.sharkdom.reseller.entity.*;
import com.sharkdom.reseller.repository.ResellerDealCustomerRepository;
import com.sharkdom.reseller.repository.ResellerDealDetailsRepository;
import com.sharkdom.reseller.repository.ResellerDealLicenseRepository;
import com.sharkdom.reseller.utill.ResellerDealDetailsMapper;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.partenerDeals.hubspot.HubSpotAuthService;
import com.sharkdom.service.partenerDeals.hubspot.HubSpotSyncService;
import com.sharkdom.service.partenerDeals.hubspot.dto.CreateDealRequest;
import com.sharkdom.service.partenerDeals.hubspot.dto.HubSpotDealPropertyRequest;
import com.sharkdom.service.partenerDeals.hubspot.dto.TokenResponse;
import com.sharkdom.util.Util;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
public class ResellerService {

    @Autowired
    private ResellerDealDetailsRepository repository;

    @Autowired
    private ResellerDealDetailsMapper mapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PartnerTierRepository partnerTierRepository;

    @Autowired
    private OrganizationUserMappingRepository organizationUserMappingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResellerDealCustomerRepository customerRepository;

    @Autowired
    private ResellerDealLicenseRepository licenseRepository;

    @Autowired
    private IntegrationRepository integrationRepository;

    @Autowired
    private HubSpotAuthService hubSpotAuthService;

    @Autowired
    private HubSpotSyncService hubSpotSyncService;

    @Autowired
    private EmailService emailService;


    public ResellerDealDetailsResponse createDeal(ResellerDealDetailsRequest request) {
        var resellerOrgId = Util.getOrgIdFromToken();
        log.info("Creating Dea Details for resellerOrgId: {}",resellerOrgId);
        try {
            ResellerDealDetails resellerDealDetails=new ResellerDealDetails();
            resellerDealDetails.setVendorOrgId(request.getVendorOrgId());
            resellerDealDetails.setResellerOrgId(resellerOrgId);
            resellerDealDetails.setResellerDealSource(ResellerDealSource.PORTAL);
            resellerDealDetails.setResellerDealStatus(ResellerDealStatus.PENDING);
            resellerDealDetails.setResellerDealStag(ResellerDealStag.WAITING_FOR_APPROVAL);
            var optionalVendorOrganization = organizationRepository.findById(request.getVendorOrgId());
            if (optionalVendorOrganization.isPresent())
            {
                Organization organization = optionalVendorOrganization.get();
                resellerDealDetails.setPartnerName(organization.getName());
                resellerDealDetails.setWebsite(organization.getWebsite());
            } else {
                resellerDealDetails.setPartnerName("");
                log.error("Organization with ID: {} not found", request.getVendorOrgId());
            }
            resellerDealDetails.setExpectedReleaseTime(request.getExpectedReleaseTime());
            resellerDealDetails.setExpectedReleaseDate(request.getExpectedReleaseDate());
            resellerDealDetails.setResellerMode(request.getResellerMode());
            resellerDealDetails.setProductPlanRequired(request.getProductPlanRequired());
            resellerDealDetails.setNumberOfLicences(request.getNumberOfLicences());
            resellerDealDetails.setActualPrice(request.getActualPrice());
            resellerDealDetails.setBuyPrice(request.getBuyPrice());
            resellerDealDetails.setCalculatedPartnerTier(request.getCalculatedPartnerTier());
            resellerDealDetails.setBillingModel(request.getBillingModel());
            var optUser = userRepository.findByEmail(Util.getUserFromToken());
            if (optUser.isPresent())
            {
                var user = optUser.get();
                resellerDealDetails.setPoc(user.getName());
            } else {
                resellerDealDetails.setPoc("");
                log.info("User with email: {} not found", Util.getUserFromToken());
            }
            var savedEntity = repository.save(resellerDealDetails);
            log.debug("Created Licence Details with ID: {} successfully", savedEntity.getId());
            return mapper.toResponse(savedEntity);
        } catch (Exception ex) {
            log.error("Error occurred while creating licence details for orgId: {} | Error: {}",
                    resellerOrgId, ex.getMessage(), ex);
            throw ex;
        }
    }

    public ResellerDealDetailsResponse getDealDetails(Long id) {
        log.info("Fetching Licence Details for ID: {}", id);
        var resellerDeal = repository.findById(id).orElseThrow(() -> {
            log.error("Licence with ID: {} not found", id);
            return new RuntimeException("Licence not found");
        });
        log.debug("Fetched Licence ID: {} successfully", resellerDeal.getId());
        return mapper.toResponse(resellerDeal);
    }

    public PartnerTierCalculatedResponse getPartnerTierCalculation(Long orgId, Long numberOfLicences) {
        log.info("Calculating Partner Tier for OrgId: {} with seats: {}", orgId, numberOfLicences);

        Optional<PartnerTier> optionalTier =
                partnerTierRepository
                        .findTopByOrgIdAndSeatLowerLessThanEqualAndSeatUpperGreaterThanEqualAndActiveTrue(
                                orgId,
                                numberOfLicences.intValue(),
                                numberOfLicences.intValue()
                        );

        if (optionalTier.isEmpty()) {
            log.warn("No Partner Tier found for OrgId: {} with seats: {}", orgId, numberOfLicences);
            return null; //  return null instead of throwing exception
        }

        PartnerTier tier = optionalTier.get();

        double actualPrice = tier.getPrice();
        double discountPercent = tier.getDiscountPercent() != null ? tier.getDiscountPercent() : 0;
        double buyPrice = actualPrice - (actualPrice * discountPercent / 100);

        PartnerTierCalculatedResponse response = new PartnerTierCalculatedResponse();
        response.setTierName(tier.getTierName());
        response.setCurrency(tier.getCurrency());
        response.setActualPrice(actualPrice);
        response.setBuyPrice(buyPrice);

        return response;
    }

    public List<ResellerDealStatusCountDto> getResellerDealCountByStatusForOrg() {
        long organizationId=Util.getOrgIdFromToken();
        log.info("Fetching Reseller Deal counts by status for OrgId: {}", organizationId);
        Map<ResellerDealStatus, Long> statusCountMap = new EnumMap<>(ResellerDealStatus.class);

        // Initialize all enum values with 0
        for (ResellerDealStatus status : ResellerDealStatus.values()) {
            statusCountMap.put(status, 0L);
        }

        // Fetch from DB
        List<Object[]> rawCounts = repository
                .getResellerDealCountsByStatusForOrg(organizationId);

        for (Object[] row : rawCounts) {
            ResellerDealStatus status = (ResellerDealStatus) row[0];
            Long count = (Long) row[1];
            statusCountMap.put(status, count);
        }

        List<ResellerDealStatusCountDto> orderedCounts = new ArrayList<>();

        // Add in fixed order (edit as per your enum order)
        orderedCounts.add(new ResellerDealStatusCountDto("ACTIVE", statusCountMap.get(ResellerDealStatus.ACTIVE)));
        orderedCounts.add(new ResellerDealStatusCountDto("PENDING", statusCountMap.get(ResellerDealStatus.PENDING)));
        orderedCounts.add(new ResellerDealStatusCountDto("EXPIRED", statusCountMap.get(ResellerDealStatus.EXPIRED)));
        orderedCounts.add(new ResellerDealStatusCountDto("REJECTED", statusCountMap.get(ResellerDealStatus.REJECTED)));

        // ALL Count
        long totalCount = statusCountMap.values().stream().mapToLong(Long::longValue).sum();
        orderedCounts.add(new ResellerDealStatusCountDto("ALL", totalCount));
        log.info("Total Count: {}", totalCount);
        return orderedCounts;
    }

    public ResellerLicenseDetailsDto getLicenseDetails(Long dealId) {

        log.info("Fetching License Details for dealId={}", dealId);

        ResellerDealDetails deal = repository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found with id " + dealId));

        Long purchased = deal.getNumberOfLicences();       // from DB
        Long allocated = 0L;                               // if no allocated table
        Long consumed = 0L;                                // if no consumption table
        Long remaining = purchased - allocated;

        return new ResellerLicenseDetailsDto(
                purchased,
                allocated,
                remaining,
                consumed
        );
    }

    public List<ResellerDealDetails> getCreateDeals(ResellerDealStatus dealStage){
        getAndUpdateExpiration(repository.findByResellerOrgId(Util.getOrgIdFromToken()));
        return repository.findByResellerOrgIdAndResellerDealStatus(Util.getOrgIdFromToken(),dealStage);
    }


    public List<ResellerDealDetails> getReceivedDeals(ResellerDealStatus dealStage){
        getAndUpdateExpiration(repository.findByVendorOrgId(Util.getOrgIdFromToken()));
        return repository.findByVendorOrgIdAndResellerDealStatus(Util.getOrgIdFromToken(),dealStage);
    }

    private List<ResellerDealDetails> getAndUpdateExpiration(List<ResellerDealDetails> deals) {
        List<ResellerDealDetails> updatedDeals = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (ResellerDealDetails deal : deals) {
            if (deal.getResellerDealStag() != ResellerDealStag.EXPIRED && deal.getExpectedReleaseTime() != null && deal.getCreationTimestamp() != null) {
                LocalDate creationDate = deal.getCreationTimestamp().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate expectedCloseDate = creationDate.plusDays(deal.getExpectedReleaseTime());

                if (expectedCloseDate.isBefore(today)) {
                    deal.setResellerDealStag(ResellerDealStag.EXPIRED);
                    updatedDeals.add(deal);
                }
            }
        }
        if (!updatedDeals.isEmpty()) {
            repository.saveAll(updatedDeals);
        }
        return updatedDeals;
    }

    public ResellerDealStageResponse getDealsForStage(ResellerDealStatus dealStage) {

        List<ResellerDealDetails> created = getCreateDeals(dealStage);
        List<ResellerDealDetails> received = getReceivedDeals(dealStage);

        return new ResellerDealStageResponse(created, received);
    }


    private ResellerDealCustomerResponse mapToResponse(ResellerDealCustomer entity) {
        ResellerDealCustomerResponse dto = new ResellerDealCustomerResponse();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setCustomerName(entity.getCustomerName());
        dto.setResellerDealId(entity.getResellerDealId());
        return dto;
    }


    public ResellerDealCustomerResponse addResellerDealCustomer(ResellerDealCustomerRequest request) {
        ResellerDealCustomer customer = new ResellerDealCustomer();
        customer.setEmail(request.getEmail());
        customer.setCustomerName(request.getCustomerName());
        customer.setResellerDealId(request.getResellerDealId());
        ResellerDealCustomer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

    public ResellerDealCustomerResponse getCustomerById(Long id) {
        ResellerDealCustomer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return mapToResponse(customer);
    }


    public Page<ResellerDealCustomerResponse> getCustomersByDealId(Long dealId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ResellerDealCustomer> customerPage =
                customerRepository.findByResellerDealId(dealId, pageable);

        return customerPage.map(this::mapToResponse);
    }


    public LicenseAllocateResponse allocateLicense(LicenseAllocateRequest request) {

        ResellerDealCustomer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, request.getValidityInDays());
        Date expiryDate = cal.getTime();

        // 2. Insert new license in license table
        ResellerDealLicense license = new ResellerDealLicense();
        license.setLicenseKey(generateLicenseKey());
        license.setCustomerEmail(customer.getEmail());
        license.setCustomerId(customer.getId());
        license.setDealId(customer.getResellerDealId());
        license.setExpiryDate(expiryDate);
        license.setStatus(LicenseStatus.ASSIGNED);

        var save = licenseRepository.save(license);

        // send notification email to customer
        emailService.sendLicenseAllocationEmail("allocatedLicense", customer.getEmail(),
                Map.of("customer_name", customer.getCustomerName(),
                        "license_key", save.getLicenseKey(),
                        "expiry_date", expiryDate.toString())
        );

        // Response
        LicenseAllocateResponse response = new LicenseAllocateResponse();
        response.setCustomerId(customer.getId());
        response.setEmail(customer.getEmail());
        response.setLicenseKey(license.getLicenseKey());
        response.setExpiryDate(expiryDate);
        response.setLicenseStatus(LicenseStatus.ASSIGNED);

        return response;
    }

    public String generateLicenseKey() {
        return "LIC-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public ResellerDealCustomerResponse updateCustomer(
            Long id,
            ResellerDealCustomerRequest request) {

        ResellerDealCustomer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setEmail(request.getEmail());
        customer.setCustomerName(request.getCustomerName());

        ResellerDealCustomer updated = customerRepository.save(customer);
        return mapToResponse(updated);
    }

    public void deleteCustomer(Long id) {
        ResellerDealCustomer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customerRepository.delete(customer);
    }

    public Page<ResellerDealCustomerResponse> getCustomerByDealId(
            Long dealId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ResellerDealCustomer> customerPage =
                customerRepository.findByResellerDealId(dealId, pageable);

        return customerPage.map(this::mapToRespons);
    }

    private ResellerDealCustomerResponse mapToRespons(ResellerDealCustomer customer) {

        ResellerDealCustomerResponse response = new ResellerDealCustomerResponse();
        response.setCustomerId(customer.getId());
        response.setEmail(customer.getEmail());
        response.setCustomerName(customer.getCustomerName());

        Optional<ResellerDealLicense> licenseOpt =
                licenseRepository.findByCustomerId(customer.getId());

        if (licenseOpt.isPresent()) {
            ResellerDealLicense license = licenseOpt.get();

            response.setLicenseAssigned(true);
            response.setLicenseKey(license.getLicenseKey());
            response.setLicenseStatus(license.getStatus());
            response.setExpiryDate(license.getExpiryDate());

            long expiryInDays = ChronoUnit.DAYS.between(
                    LocalDate.now(),
                    license.getExpiryDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
            );
            response.setExpiryInDays(expiryInDays);

        } else {
            response.setLicenseAssigned(false);
        }

        return response;
    }

    @Transactional
    public ResellerDealDetails updateApprovalStatus(Long dealId, Boolean isApproved,IntegrationType integrationType) {

        if (dealId == null) {
            throw new IllegalArgumentException("Deal ID must not be null");
        }
        if (isApproved == null) {
            throw new IllegalArgumentException("Approval status must not be null");
        }
        log.info("Updating Reseller Deal approval for ID: {} to {}", dealId, isApproved);
        ResellerDealDetails deal = repository.findById(dealId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Reseller Deal not found with ID: " + dealId));
        if (isApproved) {
            deal.setResellerDealStag(ResellerDealStag.APPROVED);
            deal.setResellerDealStatus(ResellerDealStatus.ACTIVE);
        } else {
            deal.setResellerDealStag(ResellerDealStag.WAITING_FOR_APPROVAL);
            deal.setResellerDealStatus(ResellerDealStatus.REJECTED);
        }
        ResellerDealDetails updatedDeal = repository.save(deal);
        log.info("Reseller Deal updated successfully with ID: {}", updatedDeal.getId());

        // Sync to HubSpot if approved
        if (isApproved) {
//            if (integrationType == IntegrationType.SALESFORCE) {
//                // Fetch refresh token
//                IntegrationDetails integrationDetails = integrationRepository
//                        .findByOrganizationIdAndIntegrationType(updatedDeal.getVendorOrgId(), IntegrationType.SALESFORCE);
//                SalesforceTokenResponse salesforceTokenResponse = salesforceAuthService.refreshAccessToken(integrationDetails.getRefreshToken());
//                String accessToken = salesforceTokenResponse.accessToken();
//                String instanceUrl = salesforceTokenResponse.instanceUrl();
//                log.info("Access Token for Salesforce: {}", accessToken);
//                String newDealIdFromSalesforce = salesforceSyncService.createDeal(instanceUrl, accessToken, updatedDeal);
//                log.info("Deal created on Salesforce for Deal ID: {}", updatedDeal.getDealId());
//                updatedDeal.setSalesforceDealId(newDealIdFromSalesforce);
//                Deal save = dealRepository.save(updatedDeal);
//                log.info("Deal updated in database after Salesforce creation: {}", save.getSalesforceDealId());
//            }

//            if (integrationType == IntegrationType.ZOHO) {
//                // Fetch refresh token
//                IntegrationDetails integrationDetails = integrationRepository
//                        .findByOrganizationIdAndIntegrationType(updatedDeal.getVendorOrgId(), IntegrationType.ZOHO);
//                String accessToken = zohoAuthService.refreshAccessToken(integrationDetails.getRefreshToken());
//                String zohoDealSyncServiceDealId = zohoDealSyncService.createDeal(accessToken, updatedDeal);
//                log.info("Deal created on Zoho for Deal ID: {}",zohoDealSyncServiceDealId );
//                updatedDeal.setZohoDealId(zohoDealSyncServiceDealId);
//                Deal save = dealRepository.save(updatedDeal);
//                log.info("Deal updated in database after Zoho creation: {}", save.getZohoDealId());
//            }


            if (integrationType == IntegrationType.HUBSPOT) {
                var optionalVendorOrganization = organizationRepository.findById(updatedDeal.getVendorOrgId());
                if (optionalVendorOrganization.isPresent()) {
                    Organization organization = optionalVendorOrganization.get();

                    // Fetch refresh token
                    IntegrationDetails integrationDetails = integrationRepository
                            .findByOrganizationIdAndIntegrationType(updatedDeal.getVendorOrgId(), IntegrationType.HUBSPOT);

                    String refreshToken = integrationDetails.getRefreshToken();
                    log.info("Using refresh token for HubSpot: {}", refreshToken);
                    // Step 1: Get Access Token
                    TokenResponse accessTokenUsingRefreshToken = hubSpotAuthService.getAccessTokenUsingRefreshToken(refreshToken);
                    String accessToken = accessTokenUsingRefreshToken.getAccessToken();
                    log.info("Access Token for HubSpot: {}", accessToken);

                    // Step 2: Prepare Deal Payload
                    CreateDealRequest hubspotDeal = new CreateDealRequest();
                    Map<String, String> properties = new HashMap<>();
                    properties.put("dealname", organization.getName());
                    properties.put("dealstage", "appointmentscheduled");
                    properties.put("pipeline", "default");
                    properties.put("amount", "0");
                    properties.put("dealtype", "newbusiness"); // deal with a new customer, exiting business, etc.

                    hubspotDeal.setProperties(properties);

                    // Step 3: Create Deal on HubSpot
                    try {
                        String hubspotResponse = hubSpotSyncService.createDeal(hubspotDeal, accessToken);
                        log.info("Deal created on HubSpot: {}", hubspotResponse);
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode root = objectMapper.readTree(hubspotResponse);
                        String hubspotDealId = root.path("id").asText();
                        log.info("HubSpot Deal ID: {}", hubspotDealId);
                        // Update the deal with HubSpot ID
                        updatedDeal.setHubspotDealId(hubspotDealId);
                        ResellerDealDetails updatedAfterDealCreation = repository.save(updatedDeal);

                        HubSpotDealPropertyRequest request = new HubSpotDealPropertyRequest();
                        request.setName("actual_price");
                        request.setLabel("Actual Price");
                        request.setType("string");
                        request.setFieldType("text");
                        request.setGroupName("dealinformation");
                        request.setDescription("Internal reseller identifier");
                        request.setHidden(false);
                        String response = hubSpotSyncService.createDealProperty(request, accessToken);
                        log.info("HubSpot Deal Property Creation Response: {}", response);

                        HubSpotDealPropertyRequest request1 = new HubSpotDealPropertyRequest();
                        request1.setName("buy_price");
                        request1.setLabel("buy Price");
                        request1.setType("string");
                        request1.setFieldType("text");
                        request1.setGroupName("dealinformation");
                        request1.setDescription("Internal reseller identifier");
                        request1.setHidden(false);
                        String response1 = hubSpotSyncService.createDealProperty(request1, accessToken);
                        log.info("HubSpot Deal Property Creation Response: {}", response1);

//                        HubSpotDealPropertyRequest request2 = new HubSpotDealPropertyRequest();
//                        request2.setName("buy_price");
//                        request2.setLabel("buy Price");
//                        request2.setType("string");
//                        request2.setFieldType("text");
//                        request2.setGroupName("dealinformation");
//                        request2.setDescription("Internal reseller identifier");
//                        request2.setHidden(false);
//                        String response2 = hubSpotSyncService.createDealProperty(request2, accessToken);
//                        log.info("HubSpot Deal Property Creation Response: {}", response2);

                         // Update the deal with actual price and buy price
                        log.info("Deal updated in database after HubSpot creation: {}", updatedAfterDealCreation.getId());
                    } catch (Exception e) {
                        log.error("Failed to create deal on HubSpot", e);
                        // Optional: throw exception or continue
                    }
                }
            }
        }
        return updatedDeal;
    }

    public void validateVendorConflict(Long resellerOrgId, Long vendorOrgId) {

        List<ResellerDealDetails> existingDeals =
                repository.findByVendorOrgId(vendorOrgId);

        if (existingDeals.isEmpty()) {
            return; // No conflict
        }

        boolean alreadyWithYou = existingDeals.stream()
                .anyMatch(deal -> deal.getResellerOrgId().equals(resellerOrgId));

        if (alreadyWithYou) {
            throw new ServiceException(ErrorMessages.SH175);
        }

        throw new ServiceException(ErrorMessages.SH176);
    }






}
