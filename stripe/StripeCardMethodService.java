package com.sharkdom.service.stripe;

import com.sharkdom.dto.SetupIntentResponse;
import com.sharkdom.model.stripe.StripeCardDetailDto;
import com.stripe.exception.StripeException;
import com.stripe.model.SetupIntent;
import org.springframework.stereotype.Service;

public interface StripeCardMethodService {

    StripeCardDetailDto getAndSaveCustomerCardDetails(String customerId) throws StripeException;

    StripeCardDetailDto updatePaymentMethod(String customerId, String paymentMethodId) throws StripeException;

//    Map<String, Object> getSubscriptionCardDetails(String subscriptionId) throws StripeException;

    public SetupIntentResponse createSetupIntent(String customerId) throws StripeException;

}
