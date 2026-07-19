package com.sharkdom.service.stripe;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.stripe.StripeInvoice;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.mapper.stripe.StripeInvoiceMapper;
import com.sharkdom.model.stripe.StripeInvoiceDto;
import com.sharkdom.repository.stripe.StripeInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StripeInvoiceServiceImpl implements StripeInvoiceService{

    private final StripeInvoiceRepository stripeInvoiceRepository;

    private final StripeInvoiceMapper stripeInvoiceMapper;

    @Override
    @Transactional
    public StripeInvoiceDto getInvoiceByInvoiceId(String invoiceId) {
        StripeInvoice stripeInvoice = stripeInvoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH154, invoiceId));
        return stripeInvoiceMapper.stripeInvoiceToStripeInvoiceDto(stripeInvoice);
    }

    @Override
    @Transactional
    public List<StripeInvoiceDto> getAllInvoiceBySubscriptionId(String subscriptionId) {
        List<StripeInvoice> stripeInvoices = stripeInvoiceRepository.findAllBySubscriptionId(subscriptionId);
        return stripeInvoiceMapper.stripeInvoiceListToStripeInvoiceDtoList(stripeInvoices);
    }
}
