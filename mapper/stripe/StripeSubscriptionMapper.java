package com.sharkdom.mapper.stripe;

import com.sharkdom.entity.stripe.StripeSubscriptionData;
import com.sharkdom.model.stripe.StripeSubscriptionDataDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {StripePriceMapper.class})
public interface StripeSubscriptionMapper {

    @Mapping(target = "customer.checkoutSessions", ignore = true)
    @Mapping(target = "customer.subscriptions", ignore = true)
    @Mapping(target = "customer.cardDetails", ignore = true)
    @Mapping(target = "price.subscriptions", ignore = true)
    @Mapping(target = "price.lineItems", ignore = true)
    @Mapping(target = "checkoutSession.subscriptionData", ignore = true)
    @Mapping(target = "checkoutSession.customer", ignore = true)
    @Mapping(target = "checkoutSession.lineItems", ignore = true)
//    @Mapping(target = "checkoutSession.coupons", ignore = true)
    StripeSubscriptionDataDto stripeSubscriptionDataToStripeSubscriptionDataDto(StripeSubscriptionData subscriptionData);


    StripeSubscriptionData stripeSubscriptionDataDtoToStripeSubscriptionData(StripeSubscriptionDataDto subscriptionDataDto);

    @Mapping(target = "customer.checkoutSessions", ignore = true)
    @Mapping(target = "customer.subscriptions", ignore = true)
    @Mapping(target = "customer.cardDetails", ignore = true)
    @Mapping(target = "price.subscriptions", ignore = true)
    @Mapping(target = "price.lineItems", ignore = true)
    @Mapping(target = "checkoutSession.subscriptionData", ignore = true)
    @Mapping(target = "checkoutSession.customer", ignore = true)
    @Mapping(target = "checkoutSession.lineItems", ignore = true)
//    @Mapping(target = "checkoutSession.coupons", ignore = true)
    List<StripeSubscriptionDataDto> stripeSubscriptionDataListToStripeSubscriptionDataDtoList(List<StripeSubscriptionData> subscriptionData);



}
