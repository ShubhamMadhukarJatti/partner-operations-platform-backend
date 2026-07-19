package com.sharkdom.mapper.stripe;

import com.sharkdom.entity.stripe.StripeCustomer;
import com.sharkdom.model.stripe.StripeCustomerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {StripeCheckoutSessionsMapper.class, StripeSubscriptionMapper.class, StripeCardDetailsMapper.class, StripeCouponMapper.class})
public interface StripeCustomerMapper {

    StripeCustomer customerDtoToCustomer(StripeCustomerDto customerDto);

    @Mapping(target = "subscriptions.customer", ignore = true)
    @Mapping(target = "subscriptions.checkoutSession", ignore = true)
    @Mapping(target = "checkoutSessions.customer", ignore = true)
    @Mapping(target = "checkoutSessions.subscriptionData", ignore = true)
    @Mapping(target = "checkoutSessions.coupons.checkoutSession", ignore = true)
    @Mapping(target = "cardDetails.customer", ignore = true)
    StripeCustomerDto customerToCustomerDto(StripeCustomer stripeCustomer);


    @Mapping(target = "subscriptions.customer", ignore = true)
    @Mapping(target = "subscriptions.checkoutSession", ignore = true)
    @Mapping(target = "checkoutSessions.customer", ignore = true)
    @Mapping(target = "checkoutSessions.subscriptionData", ignore = true)
    @Mapping(target = "checkoutSessions.coupons.checkoutSession", ignore = true)
    @Mapping(target = "cardDetails.customer", ignore = true)
    List<StripeCustomerDto> customerListToCustomerDtoList(List<StripeCustomer> stripeCustomers);

}
