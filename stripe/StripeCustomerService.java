package com.sharkdom.service.stripe;

import com.sharkdom.model.stripe.StripeCustomerDto;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;


public interface StripeCustomerService {

    StripeCustomerDto createCustomer(@Valid StripeCustomerDto stripeCustomerDto) throws StripeException;

    List<StripeCustomerDto> getAllCustomers();

    StripeCustomerDto getCustomerByCustomerId(@Valid String customerId);

    StripeCustomerDto getStripeCustomerDtoByUserId(StripeCustomerDto customerDto);

    public StripeCustomerDto getStripeCustomerDtoByUserId(String userId);
}
