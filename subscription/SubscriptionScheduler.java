package com.sharkdom.service.subscription;

import com.sharkdom.model.email.TemplateOrganizationEmailReqModel;
import com.sharkdom.repository.subscription.SubscriptionRepository;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.util.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class SubscriptionScheduler {

    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    DateUtil dateUtil;
    private final SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    public SubscriptionScheduler(SubscriptionRepository subscriptionRepository, EmailService emailService) {
        this.subscriptionRepository = subscriptionRepository;
        this.emailService = emailService;
    }

    //everyday 3 pm jib run to check subscription
    @Scheduled(cron = "0 0 15 * * *")
    public void triggerBefore7DaysOfSubscriptionExpiration() {
        LocalDate localDate = LocalDate.now().plusDays(7);
        var subscriptionExpiring = subscriptionRepository.findOrgIdForSubscriptionExpiriration(localDate);
        subscriptionExpiring.forEach(subscription -> {
            emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                    .subscriptionRenewal(subscription.getEndOn())
                    .subscriptionName(subscription.getPlanCode())
                    .organizationIds(List.of(subscription.getOrganizationId()))
                    .templateCode("subsExpiringNotify")
                    .build(), null, 1L, 1L);
        });

    }

    @Scheduled(cron = "0 30 15 * * *")
    public void triggerForSubscriptionExpired() {
        LocalDate localDate = LocalDate.now().minusDays(1);
        var subscriptionExpiring = subscriptionRepository.findOrgIdForSubscriptionExpiriration(localDate);
        subscriptionExpiring.forEach(subscription -> {
            emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder().subscriptionRenewal(subscription.getEndOn()).subscriptionName(subscription.getPlanCode()).organizationIds(List.of(subscription.getOrganizationId())).templateCode("subsExpiredNotify").build(), null, 1L, 1L);
        });
    }

    /*@Scheduled(cron = "0 01 19 * * *")
    public void triggerFor() {
        emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                .templateCode("partnership_ackwoledgement")
                .partnershipAcceptTime(LocalDate.now())
                .partnerOrganizationName("test1")
                .organizationIds(List.of(77L)).build(), null);
    }*/
}
