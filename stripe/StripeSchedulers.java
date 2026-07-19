package com.sharkdom.service.stripe;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.stripe.StripeSubscriptionStatus;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.stripe.StripeSubscriptionRepository;
import com.sharkdom.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripeSchedulers {

    private final StripeSubscriptionRepository stripeSubscriptionRepository;

    private final OrganizationRepository organizationRepository;

    private final EmailService emailService;

    private static final String SUBSCRIPTION_EXPIRING_NOTIFY = "subsExpiringNotify";

    private static final String SUBSCRIPTION_EXPIRED_NOTIFY = "subsExpiredNotify";

    private static final String SUBSCRIPTION_FREE_TRAIL_EXPIRING_NOTIFY = "FreeTrialExpiringNotify";

    @Scheduled(cron = "0 45 11 * * *", zone = "Asia/Kolkata")
    public void triggerBefore3DaysOfSubscriptionExpiration() {
        LocalDate calculateDate = LocalDate.now().plusDays(3);
        sendEmailForSubscriptionStatus(calculateDate, SUBSCRIPTION_EXPIRING_NOTIFY, StripeSubscriptionStatus.ACTIVE);
        sendEmailForSubscriptionStatus(calculateDate, SUBSCRIPTION_FREE_TRAIL_EXPIRING_NOTIFY, StripeSubscriptionStatus.TRIALING);
    }

    @Scheduled(cron = "0 45 11 * * *", zone = "Asia/Kolkata")
    public void triggerForSubscriptionExpired() {
        LocalDate calculateDate = LocalDate.now().minusDays(0);
        sendEmailForSubscriptionStatus(calculateDate, SUBSCRIPTION_EXPIRED_NOTIFY, StripeSubscriptionStatus.ACTIVE);
        sendEmailForSubscriptionStatus(calculateDate, SUBSCRIPTION_EXPIRED_NOTIFY, StripeSubscriptionStatus.TRIALING);
    }

    @Scheduled(cron = "0 45 11 * * *", zone = "Asia/Kolkata")
    public void triggerForSubscriptionExpiredAfter3Days() {
        LocalDate calculateDate = LocalDate.now().minusDays(3); //1
        sendEmailForSubscriptionStatus(calculateDate, SUBSCRIPTION_EXPIRED_NOTIFY, StripeSubscriptionStatus.ACTIVE);
    }

    @Scheduled(cron = "0 45 11 * * *", zone = "Asia/Kolkata")
    public void triggerForSubscriptionExpiredAfter6Days() {
        LocalDate calculateDate = LocalDate.now().minusDays(6); //6
        sendEmailForSubscriptionStatus(calculateDate, SUBSCRIPTION_EXPIRED_NOTIFY, StripeSubscriptionStatus.ACTIVE);
    }

    @Scheduled(cron = "0 45 11 * * *", zone = "Asia/Kolkata")
    public void triggerForSubscriptionExpiredAfter9Days() {
        LocalDate calculateDate = LocalDate.now().minusDays(9); //9
        sendEmailForSubscriptionStatus(calculateDate, SUBSCRIPTION_EXPIRED_NOTIFY, StripeSubscriptionStatus.ACTIVE);
    }

    private void sendEmailForSubscriptionStatus(LocalDate calculateDate, String subscriptionExpiringNotify, StripeSubscriptionStatus status) {
        var subscriptionExpiring = stripeSubscriptionRepository.findAllByEndOnAndStatus(calculateDate, status);
        log.info("subscriptionExpiring : {}", subscriptionExpiring);
        subscriptionExpiring.forEach(subscription -> {
                    for (Long organizationId : subscription.getOrganizationId()) {
                        Organization organization = organizationRepository.findById(organizationId)
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH51, String.valueOf(subscription.getPrice().getPlanType().getPlanName())));
                        try {
                            emailService.sendEmailForSubscriptionStatus(subscriptionExpiringNotify, organization.getName(), subscription);
                        } catch (Exception e) {
                            throw new ServiceException(ErrorMessages.SH84, e.getMessage());
                        }
                    }
                }
        );
    }

}
