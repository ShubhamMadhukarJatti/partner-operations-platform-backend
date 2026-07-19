package com.sharkdom.service.subscription;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.PlanType;
import com.sharkdom.constants.subscription.SubscriptionStatus;
import com.sharkdom.entity.subscription.Subscription;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.repository.subscription.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Transactional
    public Subscription createOrUpdate(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }

    public List<Subscription> findAllByUserId(String userId) {
        return subscriptionRepository.findAllByUserId(userId);
    }

    public List<Subscription> findAllByUserIdAndStatus(String userId, SubscriptionStatus[] status) {
        return subscriptionRepository.findAllByUserIdAndStatusIn(userId, Arrays.asList(status));
    }

    public List<Subscription> findAllByOrganizationIdAndStatus(long organizationId, SubscriptionStatus[] status) {
        return subscriptionRepository.findAllByOrganizationIdAndStatusIn(organizationId, Arrays.asList(status));
    }

    public List<Subscription> findAllActiveAndFutureSubscriptions() {
        return subscriptionRepository.findAllByStatusIn(List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.FUTURE));
    }

    public List<Subscription> updateAll(List<Subscription> updatedSubscriptions) {
        return subscriptionRepository.saveAll(updatedSubscriptions);
    }

    public Subscription findByOrganizationId(Long organizationId) {
        return subscriptionRepository.findFirstByOrganizationIdOrderByCreationTimestampDesc(organizationId).orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH42, organizationId));

    }


    @Transactional
    public List<Subscription> cancelSubscription(long id, String cancellationReason) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH43, String.valueOf(id)));

        List<Subscription> subscriptions = subscriptionRepository.findAllByPaymentFkAndStatusIn(
                subscription.getPaymentFk(), List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.FUTURE));

        return subscriptionRepository.saveAll(subscriptions.stream().map(sub -> {
            sub.setCancelledOn(new Date());
            sub.setCancellationReason(cancellationReason);
            sub.setStatus(SubscriptionStatus.CANCELLED);
            return sub;
        }).collect(Collectors.toList()));
    }

    public Optional<Subscription> trialSubscription(long organizationId) {
        return subscriptionRepository.findByOrganizationIdAndPlanCode(organizationId, PlanType.STANDARD_TRIAL.name());
    }
}
