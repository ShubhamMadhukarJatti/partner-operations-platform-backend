package com.sharkdom.service.stripe;

import com.sharkdom.model.stripe.StripeInvoiceDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StripeInvoiceService {


    StripeInvoiceDto getInvoiceByInvoiceId(@Valid String invoiceId);

    List<StripeInvoiceDto> getAllInvoiceBySubscriptionId(@Valid String subscriptionId);
}
