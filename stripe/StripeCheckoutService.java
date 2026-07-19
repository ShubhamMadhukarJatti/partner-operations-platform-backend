package com.sharkdom.service.stripe;

import com.sharkdom.model.stripe.StripeCheckoutSessionsDto;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public interface StripeCheckoutService {

    StripeCheckoutSessionsDto createCheckoutSession(@Valid StripeCheckoutSessionsDto stripeCheckoutSessionsDto, @Valid boolean isBusinessCustomer) throws StripeException;

    StripeCheckoutSessionsDto getCheckoutSessionById(@Valid String checkoutSessionId);
}
