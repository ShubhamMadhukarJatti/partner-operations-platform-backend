package com.sharkdom.mapper.stripe;

import com.sharkdom.entity.stripe.StripeInvoice;
import com.sharkdom.model.stripe.StripeInvoiceDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StripeInvoiceMapper {

    StripeInvoiceDto stripeInvoiceToStripeInvoiceDto(StripeInvoice stripeInvoice);

    StripeInvoice stripeInvoiceDtoToStripeInvoice(StripeInvoiceDto stripeInvoiceDto);

    List<StripeInvoiceDto> stripeInvoiceListToStripeInvoiceDtoList(List<StripeInvoice> stripeInvoices);

}
