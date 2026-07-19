package com.sharkdom.service.organization;

import com.sharkdom.entity.organization.EmailHistory;
import com.sharkdom.model.email.TemplateOrganizationEmailReqModel;
import com.sharkdom.model.organization.OrgEmailHistoryResponse;
import com.sharkdom.model.organization.OrgSectorResponse;
import com.sharkdom.repository.admin.PartnerAlertsRepository;
import com.sharkdom.repository.organization.OrgEmailHistoryRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.service.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrganizationScheduler {
    @Value("${env}")
    private String env;
    private final OrganizationRepository organizationRepository;
    private final OrgEmailHistoryRepository orgEmailHistoryRepository;
    private final EmailService emailService;
    private final PartnerAlertsRepository partnerAlertsRepository;

    public OrganizationScheduler(OrganizationRepository organizationRepository, OrgEmailHistoryRepository orgEmailHistoryRepository, EmailService emailService, PartnerAlertsRepository partnerAlertsRepository) {
        this.organizationRepository = organizationRepository;
        this.orgEmailHistoryRepository = orgEmailHistoryRepository;
        this.emailService = emailService;
        this.partnerAlertsRepository = partnerAlertsRepository;
    }

    //Monday 9 AM
   /* @Scheduled(cron = "0 00 09 * * 1", zone = "Asia/Kolkata")
    public void mondayEmail() {
        if (!env.equalsIgnoreCase("dev")) {
            var response = partnerAlertsRepository.findByDay(Days.Monday);
            if (response != null && !response.isDisabled()) {
                sendEmailToAllOrganizations();
            }
        }
    }*/

    //Tuesday 12 PM
    /*@Scheduled(cron = "0 00 12 * * 2", zone = "Asia/Kolkata")
    public void tuesdayEmail() {
        if (!env.equalsIgnoreCase("dev")) {
            var response = partnerAlertsRepository.findByDay(Days.Tuesday);
            if (response != null && !response.isDisabled()) {
                sendEmailToAllOrganizations();
            }
        }
    }*/

    /*//Wednesday 2:30 AM
    @Scheduled(cron = "0 30 14 * * 3", zone = "Asia/Kolkata")
    public void wednesdayEmail() {
        *//*if (!env.equalsIgnoreCase("dev")) {
            var response = partnerAlertsRepository.findByDay(Days.Wednesday);
            if (response != null && !response.isDisabled()) {
                sendEmailToAllOrganizations();
            }
        }*//*
        sendEmailToAllOrganizations();
    }*/

    //Thursday 6:30 PM
    /*@Scheduled(cron = "0 30 18 * * 4", zone = "Asia/Kolkata")
    public void thursdayEmail() {
        if (!env.equalsIgnoreCase("dev")) {
            var response = partnerAlertsRepository.findByDay(Days.Thursday);
            if (response != null && !response.isDisabled()) {
                sendEmailToAllOrganizations();
            }
        }
    }*/

    //Friday 8 PM
   /* @Scheduled(cron = "0 00 20 * * 5", zone = "Asia/Kolkata")
    public void fridayEmail() {
        if (!env.equalsIgnoreCase("dev")) {
            sendEmailToAllOrganizations();
            *//*var response = partnerAlertsRepository.findByDay(Days.Friday);
            if (response != null && !response.isDisabled()) {
                sendEmailToAllOrganizations();
            }*//*
        }
    }*/

    //Saturday 2:30 PM
    /*@Scheduled(cron = "0 45 14 * * 6", zone = "Asia/Kolkata")
    public void saturdayEmail() {
        if (!env.equalsIgnoreCase("dev")) {
            var response = partnerAlertsRepository.findByDay(Days.Saturday);
            if (response != null && !response.isDisabled()) {
                sendEmailToAllOrganizations();
            }
        }
    }*/

    //Sunday 5 PM
   /* @Scheduled(cron = "0 00 17 * * 7", zone = "Asia/Kolkata")
    public void sundayEmail() {
        if (!env.equalsIgnoreCase("dev")) {
            sendEmailToAllOrganizations();
        }
    }*/

    public void sendEmailToAllOrganizations() {

        List<OrgSectorResponse> organizations = organizationRepository.findIds();
        organizations.forEach(organization -> {
            var sentOrgIds = orgEmailHistoryRepository.findAllByOrganizationId(organization.getId());
            sentOrgIds.add(organization.getId());
            OrgEmailHistoryResponse emailHistoryResponse = organizationRepository.getOrgNameAndDescription(sentOrgIds);
            sentOrgIds.add(emailHistoryResponse.getId());
            var emailHistoryResponseSector = organizationRepository.getOrgNameAndDescriptionBySector(sentOrgIds, organization.getSector());
            var reqModel = TemplateOrganizationEmailReqModel.builder()
                    .organizationIds(List.of(organization.getId()))
                    .organizationName(organization.getName())
                    .organization1Name(emailHistoryResponse.getName())
                    .organization1Logo(emailHistoryResponse.getLogoUrl())
                    .organization1Desc(emailHistoryResponse.getDescription());
            OrgEmailHistoryResponse additionalResponse;
            if (emailHistoryResponseSector == null) {
                additionalResponse = organizationRepository.getOrgNameAndDescription(sentOrgIds);
                reqModel.organization2Desc(additionalResponse.getDescription())
                        .organization2Name(additionalResponse.getName())
                        .organization2Logo(additionalResponse.getLogoUrl());
                orgEmailHistoryRepository.save(EmailHistory.builder().organizationId(organization.getId()).senderOrganizationId(additionalResponse.getId()).build());
            } else {
                reqModel.organization2Desc(emailHistoryResponseSector.getDescription())
                        .organization2Name(emailHistoryResponseSector.getName())
                        .organization2Logo(emailHistoryResponseSector.getLogoUrl());
                orgEmailHistoryRepository.save(EmailHistory.builder().organizationId(organization.getId()).senderOrganizationId(emailHistoryResponseSector.getId()).build());
            }
            String unsubscribeLink = "";
            if (env.equalsIgnoreCase("dev")) {
                unsubscribeLink = "https://dev.sharkdom.com/unsubscribe?email=" + organization.getPrimaryEmail();
            } else {
                unsubscribeLink = "https://sharkdom.com/unsubscribe?email=" + organization.getPrimaryEmail();
            }
            reqModel.emailUnsubscribeLink(unsubscribeLink);
            emailService.sendByTemplateAndOrganizationIds(reqModel.organizationCount(organizations.size()).templateCode("partner_alert").build(), null, 1L, 1L);
            orgEmailHistoryRepository.save(EmailHistory.builder().organizationId(organization.getId()).senderOrganizationId(emailHistoryResponse.getId()).build());
        });
    }
}

