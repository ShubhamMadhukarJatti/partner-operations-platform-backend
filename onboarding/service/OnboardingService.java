package com.sharkdom.onboarding.service;

import com.google.common.net.InternetDomainName;
import com.sharkdom.constants.Constants;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.*;
import com.sharkdom.dto.AutomationResponseDto;
import com.sharkdom.entity.credits.Credits;
import com.sharkdom.entity.organization.*;
import com.sharkdom.entity.user.User;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.onboarding.entity.OnboardingData;
import com.sharkdom.onboarding.jobs.OrganizationEnrichmentScheduler;
import com.sharkdom.onboarding.model.OnboardingStepRequest;
import com.sharkdom.onboarding.model.UpdateOnboardingEmailRequest;
import com.sharkdom.onboarding.repository.OnboardingDataRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.service.common.StepTrackerService;
import com.sharkdom.service.organization.OrganizationUserMappingService;
import com.sharkdom.util.UrlNormalizer;
import com.sharkdom.util.Util;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.net.URL;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OnboardingService {

    private final OnboardingDataRepository onboardingDataRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final StepTrackerService stepTrackerService;
    private final OrganizationUserMappingService organizationUserMappingService;
    private final AutomationDataService automateDataService;
    private final OrganizationEnrichmentScheduler organizationEnrichmentScheduler;

    // ========================= SAVE ONBOARDING =========================
    public OnboardingData saveOnboardingData(OnboardingStepRequest r){

        log.info("Onboarding submit started | raw companyURL={}", r.getCompanyURL());

        // Normalize URL (IMPORTANT)
        String normalizedUrl = UrlNormalizer.normalize(r.getCompanyURL());

        log.info("Normalized companyURL={}", normalizedUrl);

        OnboardingData d = OnboardingData.builder()
                .fullName(r.getFullName())
                .companyURL(normalizedUrl)
                .teamParticipation(r.getTeamParticipation())
                .marketSegment(r.getMarketSegment())
                .partnershipTeamSize(r.getGtmTeamSize())
                .currentPartners(r.getCurrentPartners())
                .goalsWithSharkdom(r.getGoalsWithSharkdom() == null ? null : String.join(",", r.getGoalsWithSharkdom()))
                .regionToPartnerWith(r.getPreferredRegion())
                .email(r.getEmail())
                .preferredPartnerships(r.getPreferredPartnerships() == null ? null : String.join(",", r.getPreferredPartnerships()))
                .build();

        var saved = onboardingDataRepository.save(d);

        log.info("Onboarding submit completed | saved companyURL={}", normalizedUrl);

        return saved;
    }

    // ========================= WEBSITE CHECK =========================
    public boolean isWebsiteAvailable(String website){
        String normalized = UrlNormalizer.normalize(website);
        return !organizationRepository.existsByWebsiteIgnoreCase(normalized);
    }

    // ========================= UPDATE EMAIL =========================
    public OnboardingData updateEmail(UpdateOnboardingEmailRequest r){
        log.info("Update onboarding email | companyURL={}", r.getCompanyURL());
        if(r.getCompanyURL()==null || r.getCompanyURL().isBlank())
            throw new ServiceException(ErrorMessages.SH05);
        if(r.getEmail()==null || r.getEmail().isBlank())
            throw new ServiceException(ErrorMessages.SH04);
        // Normalize before search
        String normalizedUrl = UrlNormalizer.normalize(r.getCompanyURL());
        OnboardingData d = onboardingDataRepository
                .findByCompanyURLIgnoreCase(normalizedUrl)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND, "Onboarding not found"));
        d.setEmail(r.getEmail());
        return onboardingDataRepository.save(d);
    }

    // ========================= FETCH USER =========================
    public User getUserByEmail(String email){
        if(email==null||email.isBlank()) throw new ServiceException(ErrorMessages.SH04);
        return userRepository.findByEmail(email).orElseThrow(()->new ServiceException(ErrorMessages.SH38,email));
    }

    // ========================= POST REGISTRATION FLOW =========================
    @Transactional
    public void handlePostRegistration(String email){
        log.info("Post-registration started | email={}",email);
        if(email==null||email.isBlank()) throw new ServiceException(ErrorMessages.SH04);

        var onboarding=onboardingDataRepository.findByEmailIgnoreCase(email).orElseThrow(()->new ServiceException(ErrorMessages.SH12));
        var user=userRepository.findByEmail(email).orElseThrow(()->new ServiceException(ErrorMessages.SH05));

        try{
            // USER UPDATE
            user.setName(onboarding.getFullName()); user.setUserType(onboarding.getTeamParticipation()); user.setOnboarded(true); user.setStatus("ACTIVE");
            User savedUser=userRepository.save(user);

            // ORGANIZATION CREATE
            Organization org= create(buildOrganizationFromOnboarding(onboarding,email));


            // USER-ORG MAPPING
            OrganizationUserMapping m=new OrganizationUserMapping(); m.setStatus(OrgUserMappingStatus.ACTIVE); m.setOrganizationId(org.getId()); m.setUserId(savedUser.getUserId()); m.setRole(OrgUserRole.ADMIN);
            var map=organizationUserMappingService.create(m);

            // ALERT
            if(map!=null) stepTrackerService.csmTeamAlert(savedUser.getUserId());

            try {
                organizationEnrichmentScheduler.scheduleEnrichment(org.getId());
            } catch (Exception ex) {
                log.error("Scheduling failed | orgId={} | pushing to retry", org.getId(), ex);
            }


            log.info("Post-registration success | email={}, userId={}, orgId={}",email,savedUser.getUserId(),org.getId());

        }catch(Exception ex){ log.error("Post-registration failed | email={}",email,ex); throw new ServiceException(ErrorMessages.SH13); }
    }


    // ========================= CREATE ORGANIZATION =========================
    @Transactional
    public Organization create(Organization org){
        String website=org.getWebsite(); if(website==null||website.isEmpty()) throw new IllegalArgumentException("Website required");
        String name=extractDomainName(website);
        if(organizationRepository.existsOrganizationByName(name)) throw new ServiceException(ErrorMessages.SH12);
        String code=name+"-"+UUID.randomUUID().toString().substring(0,4);
        if(organizationRepository.existsByCode(code)) throw new ServiceException(ErrorMessages.SH12);

        org.setName(name); org.setCode(code);
        if(org.getLogoUrl()==null) org.setLogoUrl(Constants.PLACEHOLDER_LOGO);
        org.setCredits(new Credits());

        return organizationRepository.save(org);
    }

    // ========================= DOMAIN EXTRACT =========================
    private String extractDomainName(String website){
        try {
            String normalized = UrlNormalizer.normalize(website);

            java.net.URI uri = new java.net.URI(normalized);
            String host = uri.getHost();

            InternetDomainName d = InternetDomainName.from(host).topPrivateDomain();

            return d.toString().split("\\.")[0];

        } catch(Exception e){
            throw new IllegalArgumentException("Invalid website URL");
        }
    }

    private String getOrDefault(Map<String, String> map, String key) {
        return map.getOrDefault(key, "").trim();
    }

    // ========================= BUILD ORGANIZATION =========================
    private Organization buildOrganizationFromOnboarding(OnboardingData o, String email) {

        Organization org = new Organization();

        org.setWebsite(o.getCompanyURL());
        org.setStatus(OrganizationStatus.ACTIVE);
        org.setPrimaryEmail(email);
        org.setPrimaryEmailVerified(Boolean.TRUE.toString());
        org.setSource(Source.Web);

        org.setCompanyType(o.getMarketSegment());
        org.setOpenForPartnership(true);
        org.setGoalsToUseSharkdom(o.getGoalsWithSharkdomAsList());
        org.setRegionToPartnerWith(o.getRegionToPartnerWith());
        org.setProductUrl("https://sharkdom.com/product/");
        org.setPartnershipTeamSize(o.getPartnershipTeamSize());
        org.setOnboardedPartners(o.getCurrentPartners());


        // Preferred partnerships
        if (o.getPreferredPartnerships() != null) {
            List<PreferredPartnershipTypes> list =
                    Arrays.stream(o.getPreferredPartnerships().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(t -> {
                                PreferredPartnershipTypes p = new PreferredPartnershipTypes();
                                p.setArea(t);
                                return p;
                            }).toList();

            org.setPreferredPartnershipTypes(list);
        }

        return org;
    }




}