package com.sharkdom.subscription.scheduler;

import com.sharkdom.subscription.entity.OrganizationSubscription;
import com.sharkdom.subscription.repository.OrganizationSubscriptionRepository;
import com.sharkdom.reseller.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GracePeriodScheduler {

    private final OrganizationSubscriptionRepository subscriptionRepository;

    /**
     * Runs every hour to check expired grace periods
     */
    @Scheduled(cron = "0 0 * * * *")
    public void processGraceExpiry() {

        log.info("[GRACE CHECK START] Running grace period expiry job at {}", LocalDateTime.now());

        List<OrganizationSubscription> subs =
                subscriptionRepository.findByPaymentStatus(PaymentStatus.GRACE_PERIOD);

        if (subs.isEmpty()) {
            log.info("No subscriptions in GRACE_PERIOD");
            return;
        }

        int downgradedCount = 0;

        for (OrganizationSubscription sub : subs) {

            try {
                if (sub.getGracePeriodEnd() == null) {
                    log.warn("Grace period end is null for orgId={}", sub.getOrganizationId());
                    continue;
                }

                if (sub.getGracePeriodEnd().isBefore(LocalDateTime.now())) {

                    log.info("Downgrading orgId={} | subscriptionId={} | graceEnd={}",
                            sub.getOrganizationId(),
                            sub.getStripeSubscriptionId(),
                            sub.getGracePeriodEnd());

                    sub.setPaymentStatus(PaymentStatus.FREE);
                    sub.setActiveSuites(Collections.emptyList());

                    subscriptionRepository.save(sub);

                    downgradedCount++;
                }

            } catch (Exception ex) {
                log.error("Error processing orgId={} | error={}",
                        sub.getOrganizationId(),
                        ex.getMessage(), ex);
            }
        }

        log.info("🏁 [GRACE CHECK END] Total downgraded subscriptions = {}", downgradedCount);
    }
}