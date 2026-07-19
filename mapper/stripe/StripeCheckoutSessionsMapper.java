package com.sharkdom.mapper.stripe;

import com.sharkdom.entity.stripe.StripeCheckoutSessions;
import com.sharkdom.model.stripe.StripeCheckoutSessionsDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {LineItemMapper.class, StripeSubscriptionMapper.class, StripePriceMapper.class, StripeCouponMapper.class})
public interface StripeCheckoutSessionsMapper {

    StripeCheckoutSessions stripeCheckoutSessionsDtoToStripeCheckoutSessions(StripeCheckoutSessionsDto stripeCheckoutSessionsDto);

    @Mapping(target = "customer.checkoutSessions", ignore = true)
    @Mapping(target = "customer.subscriptions", ignore = true)
    @Mapping(target = "customer.cardDetails", ignore = true)
    @Mapping(target = "lineItems.checkoutSession", ignore = true)
    @Mapping(target = "subscriptionData.checkoutSession", ignore = true)
    @Mapping(target = "subscriptionData.customer", ignore = true)
    @Mapping(target = "coupons.checkoutSession", ignore = true)
    StripeCheckoutSessionsDto stripeCheckoutSessionsToStripeCheckoutSessionsDto(StripeCheckoutSessions stripeCheckoutSessions);


}
