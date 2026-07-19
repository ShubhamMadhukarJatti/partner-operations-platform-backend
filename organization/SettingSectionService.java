package com.sharkdom.service.organization;

import com.sharkdom.dto.*;
import com.sharkdom.entity.organization.GettingStartedEntity;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.stripe.StripeInvoice;
import com.sharkdom.entity.stripe.StripeSubscriptionData;
import com.sharkdom.repository.organization.GettingStartedRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.stripe.StripeInvoiceRepository;
import com.sharkdom.repository.stripe.StripeSubscriptionRepository;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SettingSectionService {
    
    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private GettingStartedRepository gettingStartedRepository;

    @Autowired
    private StripeSubscriptionRepository subscriptionRepo;

    @Autowired
    private StripeInvoiceRepository invoiceRepo;

    @Transactional
    public Organization updateCompanyDetails(CompanyDetailsRequest request) {
        Long orgId=Util.getOrgIdFromToken();
        log.info("Updating organization details for orgId={}", orgId);
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> {
                    log.error("Organization not found for id={}", orgId);
                    return new RuntimeException("Organization not found");
                });

        // Unique name validation
        if (request.getName() != null &&
                !request.getName().equalsIgnoreCase(org.getName()) &&
                organizationRepository.existsOrganizationByName(request.getName())) {

            log.warn("Duplicate organization name attempted: {}", request.getName());
            throw new RuntimeException("Organization name already exists");
        }

        if (request.getName() != null) {
            org.setName(request.getName());
            log.debug("Updated name");
        }

        if (request.getIncorporationDate() != null) {
            org.setIncorporationDate(request.getIncorporationDate());
            log.debug("Updated inceptionYear");
        }

        if (request.getWebsite() != null) {
            org.setWebsite(request.getWebsite());
            log.debug("Updated website");
        }

        if (request.getIsInHousePartnership() != null) {
            org.setIsInHousePartnership(request.getIsInHousePartnership());
            log.debug("Updated partnership flag");
        }

        if (request.getAbout() != null) {
            org.setAbout(request.getAbout());
            log.debug("Updated about");
        }

        if (request.getAboutProductService() != null) {
            org.setBriefDescription(request.getAboutProductService());
            log.debug("Updated product description");
        }

        if (request.getProductUrl() != null) {
            org.setProductUrl(request.getProductUrl());
            log.debug("Updated product description");
        }

        Organization savedOrg = organizationRepository.save(org);

        log.info("Organization saved successfully with id={}", savedOrg.getId());

        return savedOrg;
    }

    public CompanyDetailsResponse getCompanyDetails() {

        Long orgId = Util.getOrgIdFromToken();
        log.info("Fetching organization details for orgId={}", orgId);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> {
                    log.error("Organization not found for id={}", orgId);
                    return new RuntimeException("Organization not found");
                });

        CompanyDetailsResponse response = new CompanyDetailsResponse();

        response.setName(org.getName());
        response.setIncorporationDate(org.getIncorporationDate());
        response.setWebsite(org.getWebsite());
        response.setIsInHousePartnership(org.getIsInHousePartnership());
        response.setAbout(org.getAbout());
        response.setAboutProductService(org.getBriefDescription());
        response.setProductUrl(org.getProductUrl());

        log.info("Organization details fetched successfully for orgId={}", orgId);

        return response;
    }

    @Transactional
    public Organization updatePartnershipDetails(PartnershipUpdateRequest request) {
        Long orgId=Util.getOrgIdFromToken();
        log.info("Updating organization details for orgId={}", orgId);
        log.info("Updating Organization | orgId={}", orgId);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found with id: " + orgId));

        if (request.getRegistrationType() != null)
            org.setRegistrationType(request.getRegistrationType());

        if (request.getPartnershipTeamSize() != null)
            org.setPartnershipTeamSize(request.getPartnershipTeamSize());

        if (request.getGoalsToUseSharkdom() != null)
            org.setGoalsToUseSharkdom(request.getGoalsToUseSharkdom());

        if (request.getPreferredPartnershipTypes() != null)
            org.setPreferredPartnershipTypes(request.getPreferredPartnershipTypes());

        if (request.getRegionToPartnerWith() != null)
            org.setRegionToPartnerWith(request.getRegionToPartnerWith());

        if (request.getPreferredSectors() != null)
            org.setPreferredSectors(request.getPreferredSectors());

        if (request.getOnboardedPartners()!= null)
            org.setOnboardedPartners(request.getOnboardedPartners());

        if(request.getCompanyType()!=null)
        {
            org.setCompanyType(request.getCompanyType());
        }

        if (request.getTargetMarket()!= null)
            org.setTargetMarket(request.getTargetMarket());

        Organization updated = organizationRepository.save(org);

        log.info("Organization updated successfully | orgId={}", orgId);

        return updated;
    }

    public PartnershipDetailsResponse  getPartnershipDetails() {

        Long orgId = Util.getOrgIdFromToken();

        log.info("Fetching partnership details | orgId={}", orgId);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found with id: " + orgId));

        PartnershipDetailsResponse response = new PartnershipDetailsResponse();

        response.setRegistrationType(org.getRegistrationType());
        response.setPartnershipTeamSize(org.getPartnershipTeamSize());
        response.setGoalsToUseSharkdom(org.getGoalsToUseSharkdom());
        response.setPreferredPartnershipTypes(org.getPreferredPartnershipTypes());
        response.setRegionToPartnerWith(org.getRegionToPartnerWith());
        response.setTargetMarket(org.getTargetMarket());
        response.setOnboardedPartners(org.getOnboardedPartners());
        response.setPreferredSectors(org.getPreferredSectors());
        response.setCompanyType(org.getCompanyType());

        log.info("Partnership details fetched successfully | orgId={}", orgId);

        return response;
    }

    @Transactional
    public Organization updateAddressAndContact(AddressContactUpdateRequest request) {

        Long orgId = Util.getOrgIdFromToken();
        log.info("Updating Address & Contact | orgId={}", orgId);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found with id " + orgId));

        if (request.getAddress() != null)
            org.setAddress(request.getAddress());

        if (request.getCity() != null)
            org.setCity(request.getCity());

        if (request.getZipCode() != null)
            org.setPincode(request.getZipCode());

        if (request.getState() != null)
            org.setState(request.getState());

        if (request.getCountry() != null)
            org.setCountry(request.getCountry());

        if (request.getPhone() != null)
            org.setContactNumber(request.getPhone());

        Organization saved = organizationRepository.save(org);

        log.info("Address & Contact updated successfully | orgId={}", orgId);

        return saved;
    }

    public AddressContactResponse getAddressAndContact() {
        Long orgId = Util.getOrgIdFromToken();
        log.info("Fetching Address & Contact | orgId={}", orgId);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found with id " + orgId));

        AddressContactResponse response = new AddressContactResponse();

        response.setAddress(org.getAddress());
        response.setCity(org.getCity());
        response.setZipCode(org.getPincode());
        response.setState(org.getState());
        response.setCountry(org.getCountry());
        response.setPhone(org.getContactNumber());

        return response;
    }

    @Transactional
    public GettingStartedEntity updateIppDetails(GettingStartedUpdateRequest request) {
        Long orgId = Util.getOrgIdFromToken();
        log.info("Updating Getting Started section | orgId={}", orgId);

        GettingStartedEntity entity = gettingStartedRepository.findByOrganizationId(orgId)
                .orElseGet(() -> {
                    log.info("No record found, creating new one for orgId={}", orgId);
                    return GettingStartedEntity.builder()
                            .organizationId(orgId)
                            .build();
                });
        if (request.getBrandingPage() != null)
            entity.setBrandingPage(request.getBrandingPage());

        if (request.getActivePartnerProgram() != null)
            entity.setActivePartnerProgram(request.getActivePartnerProgram());

        GettingStartedEntity saved = gettingStartedRepository.save(entity);

        log.info("Getting Started updated | orgId={}", orgId);

        return saved;
    }


    public GettingStartedResponse getIPPDetails() {
        Long orgId = Util.getOrgIdFromToken();
        log.info("Fetching Getting Started | orgId={}", orgId);

        GettingStartedEntity entity = gettingStartedRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new RuntimeException("Getting Started not found for orgId=" + orgId));

        GettingStartedResponse response = new GettingStartedResponse();
        response.setBrandingPage(entity.getBrandingPage());
        response.setActivePartnerProgram(entity.getActivePartnerProgram());

        return response;
    }


    public List<StripeSubscriptionWithInvoiceResponse> getSubscriptionAndInvoicesByOrg() {
        Long orgId = Util.getOrgIdFromToken();
        log.info("Fetching subscription for orgId: {}", orgId);
        // Step 1: Find subscriptions by organizationId
        List<StripeSubscriptionData> subscriptions = subscriptionRepo.findByOrganizationId(orgId);
        List<StripeSubscriptionWithInvoiceResponse> finalList = new ArrayList<>();
        for (StripeSubscriptionData sub : subscriptions) {

            String subscriptionId = sub.getSubscriptionId();

            // Step 2: Fetch invoices mapped with subscriptionId
            List<StripeInvoice> invoiceList = invoiceRepo.findBySubscriptionId(subscriptionId);

            // Step 3: Build response DTO
            StripeSubscriptionWithInvoiceResponse resp = new StripeSubscriptionWithInvoiceResponse();
            resp.setSubscriptionData(sub);
            resp.setInvoices(invoiceList);

            finalList.add(resp);
        }
        return finalList;
    }


    public List<InvoiceWithSubDetailsResponse> getInvoicesByOrg() {

        Long orgId = Util.getOrgIdFromToken();

        // Step 1: Find subscriptions for org
        List<StripeSubscriptionData> subscriptions = subscriptionRepo.findByOrganizationId(orgId);

        List<InvoiceWithSubDetailsResponse> finalList = new ArrayList<>();

        for (StripeSubscriptionData sub : subscriptions) {

            // Step 2: Fetch invoices for subscription
            List<StripeInvoice> invoices = invoiceRepo.findBySubscriptionId(sub.getSubscriptionId());

            // Step 3: Convert each invoice into new DTO
            for (StripeInvoice invoice : invoices) {
                InvoiceWithSubDetailsResponse dto = new InvoiceWithSubDetailsResponse(
                        invoice,
                        sub.getAmount(),       // ← sending amount
                        sub.getCreatedAt()     // ← sending createdAt
                );
                finalList.add(dto);
            }
        }

        return finalList;
    }




}
