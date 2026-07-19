package com.sharkdom.service.stripe;

import com.sharkdom.constants.stripe.StripeMode;
import com.sharkdom.constants.stripe.StripePlanType;
import com.sharkdom.model.stripe.StripeCheckoutSessionsDto;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public interface StripeCustomerCheckoutService {

    StripeCheckoutSessionsDto createCustomerAndCheckoutSession(@Valid String userId,
                                                               @Valid StripePlanType planType,
                                                               @Valid StripeMode mode,
                                                               @Valid Long trailDays,
                                                               @Valid String successUrl,
                                                               @Valid String cancelUrl,
                                                               @Valid String couponCode,
                                                               @Valid boolean isBusinessCustomer) throws StripeException;
}
