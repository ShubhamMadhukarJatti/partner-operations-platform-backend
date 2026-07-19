package com.sharkdom.subscription.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.reseller.entity.PaymentStatus;
import com.sharkdom.subscription.entity.OrganizationSubscription;
import com.sharkdom.subscription.entity.SuiteKey;
import com.sharkdom.subscription.model.AccessResponse;
import com.sharkdom.subscription.repository.OrganizationSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureAccessService {

    private final OrganizationSubscriptionRepository subscriptionRepository;

    public AccessResponse canAccess(Long orgId, SuiteKey requiredSuite, boolean isFreeFeature) {

        log.info("[ACCESS CHECK] orgId={} | suite={} | isFreeFeature={}",
                orgId, requiredSuite, isFreeFeature);

        OrganizationSubscription sub = subscriptionRepository
                .findByOrganizationId(orgId)
                .orElseThrow(() -> {
                    log.error("[ACCESS ERROR] Subscription not found for orgId={}", orgId);
                    return new ServiceException(ErrorMessages.SH42, orgId);
                });

        log.info("[SUBSCRIPTION STATE] orgId={} | paymentStatus={} | activeSuites={}",
                orgId, sub.getPaymentStatus(), sub.getActiveSuites());

        // Grace period → FULL ACCESS
        if (sub.getPaymentStatus() == PaymentStatus.GRACE_PERIOD) {
            log.info("[ACCESS GRANTED] orgId={} | reason=grace_period", orgId);
            return AccessResponse.allowed("grace_period");
        }

        // Paid active suite
        if (sub.getActiveSuites() != null &&
                sub.getActiveSuites().contains(requiredSuite)) {

            log.info("[ACCESS GRANTED] orgId={} | reason=active_suite", orgId);
            return AccessResponse.allowed("active");
        }

        // Free feature allowed
        if (isFreeFeature) {
            log.info("[ACCESS GRANTED] orgId={} | reason=free_feature", orgId);
            return AccessResponse.allowed("free");
        }

        // Denied
        log.warn("[ACCESS DENIED] orgId={} | requiredSuite={}", orgId, requiredSuite);

        return AccessResponse.denied("suite_required");
    }
}