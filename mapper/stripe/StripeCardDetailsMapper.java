package com.sharkdom.mapper.stripe;

import com.sharkdom.entity.stripe.StripeCardDetail;
import com.sharkdom.model.stripe.StripeCardDetailDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {})
public interface StripeCardDetailsMapper {

    StripeCardDetail stripeCardDetailDtoToStripeCardDetail(StripeCardDetailDto stripeCardDetailDto);

    @Mapping(target = "customer.checkoutSessions", ignore = true)
    @Mapping(target = "customer.subscriptions", ignore = true)
    @Mapping(target = "customer.cardDetails", ignore = true)
    StripeCardDetailDto stripeCardDetailToStripeCardDetailDto(StripeCardDetail stripeCardDetail);


    @Mapping(target = "customer.checkoutSessions", ignore = true)
    @Mapping(target = "customer.subscriptions", ignore = true)
    @Mapping(target = "customer.cardDetails", ignore = true)
    List<StripeCardDetailDto> stripeCardDetailListToStripeCardDetailDtoList(List<StripeCardDetail> stripeCardDetails);

}
