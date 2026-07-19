package com.sharkdom.service.stripe;

import com.sharkdom.constants.stripe.StripePlanType;
import com.sharkdom.model.stripe.CreateSubscriptionRequest;
import com.sharkdom.model.stripe.StripeSubscriptionDataDto;
import com.sharkdom.model.stripe.UpgradeResponseDTO;
import com.stripe.exception.StripeException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


public interface StripeSubscriptionService {

    StripeSubscriptionDataDto getSubscriptionBySubscriptionId(String subscriptionId) throws StripeException;

    List<StripeSubscriptionDataDto> getSubscriptionsByOrganizationId(Long organizationId);

    StripeSubscriptionDataDto cancelSubscription(String subscriptionId, boolean requestRefund) throws StripeException;

    UpgradeResponseDTO upgradeSubscription(String subscriptionId, StripePlanType planType, String successUrl,
                                           String cancelUrl) throws StripeException;

    Map<String, Object> downgradeSubscriptionV1(String subscriptionId, StripePlanType planType, String successUrl,
                                              String cancelUrl) throws StripeException;

    UpgradeResponseDTO upgradeSeat(String subscriptionId, StripePlanType planType, String successUrl, String cancelUrl)
            throws StripeException;

    Map<String, Object> downgradeSeat(String subscriptionId, StripePlanType planType, String successUrl,
                                      String cancelUrl) throws StripeException;

    StripeSubscriptionDataDto createSubscription(CreateSubscriptionRequest request) throws StripeException;


}

