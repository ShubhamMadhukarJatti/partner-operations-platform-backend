package com.sharkdom.service.stripe;


import com.sharkdom.constants.stripe.StripeMode;
import com.sharkdom.constants.stripe.StripePlanType;
import com.sharkdom.model.stripe.*;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class StripeCustomerCheckoutServiceImpl implements StripeCustomerCheckoutService {

    private final StripeCustomerService stripeCustomerService;

    private final StripeCheckoutService stripeCheckoutService;

    private final StripePlanConfigurationService stripePlanConfigurationService;

    private static final Long STRIPE_PRICE_QUANTITY = 1L;

    @Override
    public StripeCheckoutSessionsDto createCustomerAndCheckoutSession(@Valid String userId, @Valid StripePlanType planType, @Valid StripeMode mode, @Valid Long trailDays, @Valid String successUrl, @Valid String cancelUrl, @Valid String couponCode, @Valid boolean isBusinessCustomer) throws StripeException {
        StripeCustomerDto passStripeCustomerDto = new StripeCustomerDto();
        passStripeCustomerDto.setFirebaseUserId(userId);
        log.info("passStripeCustomerDto={}", passStripeCustomerDto);
        StripeCustomerDto stripeCustomerDtoByUserId = stripeCustomerService.getStripeCustomerDtoByUserId(passStripeCustomerDto);
        log.info("stripeCustomerDtoByUserId = {}", stripeCustomerDtoByUserId.toString());
        StripeCustomerDto createdCustomerResponse = stripeCustomerService.createCustomer(stripeCustomerDtoByUserId);

        StripeCheckoutSessionsDto checkoutSessionsRequest = createStripeCheckoutSessionsRequest(planType, mode, trailDays,  successUrl, cancelUrl, createdCustomerResponse, couponCode);
        log.info("CheckoutSessionsRequest: {}", checkoutSessionsRequest);
        return stripeCheckoutService.createCheckoutSession(checkoutSessionsRequest, isBusinessCustomer);
    }

    public StripeCustomerDto createCustomer(String userId) throws StripeException {

        StripeCustomerDto stripeCustomerDtoByUserId =
                stripeCustomerService.getStripeCustomerDtoByUserId(userId);

        log.info("stripeCustomerDtoByUserId = {}", stripeCustomerDtoByUserId);

        StripeCustomerDto createdCustomerResponse =
                stripeCustomerService.createCustomer(stripeCustomerDtoByUserId);

        return createdCustomerResponse;
    }

    @NotNull
    private StripeCheckoutSessionsDto createStripeCheckoutSessionsRequest(StripePlanType planType, StripeMode mode, Long trailDays, String successUrl, String cancelUrl,  StripeCustomerDto createdCustomerResponse, String couponCode) {
        return getStripeCheckoutSessionsDtoForSubscriptionModeNDaysTrail(planType, mode, trailDays, successUrl, cancelUrl, createdCustomerResponse, couponCode);
    }

    @NotNull
    private StripeCheckoutSessionsDto getStripeCheckoutSessionsDtoForSubscriptionModeNDaysTrail(StripePlanType planType, StripeMode mode, Long trailDays,  String successUrl, String cancelUrl, StripeCustomerDto createdCustomerResponse, String couponCode) {
        StripeCheckoutSessionsDto checkoutSessionsRequest = new StripeCheckoutSessionsDto();
        checkoutSessionsRequest.setCustomer(createdCustomerResponse);
        checkoutSessionsRequest.setSuccessUrl(successUrl);
        checkoutSessionsRequest.setCancelUrl(cancelUrl);
        checkoutSessionsRequest.setMode(mode);
        List<LineItemEntityDto> lineItemEntityDtoList = checkoutSessionsRequest.getLineItems();
        if(ObjectUtils.isEmpty(lineItemEntityDtoList)){
            lineItemEntityDtoList = new ArrayList<>();
        }
        LineItemEntityDto lineItemEntityDto = new LineItemEntityDto();
        PriceEntityDto priceEntityDto = new PriceEntityDto();
        priceEntityDto.setPlanType(planType);
        priceEntityDto.setStripePriceId(stripePlanConfigurationService.getPriceIdByPlanType(priceEntityDto.getPlanType()));
        lineItemEntityDto.setPrice(priceEntityDto);
        lineItemEntityDto.setQuantity(STRIPE_PRICE_QUANTITY);
        lineItemEntityDtoList.add(lineItemEntityDto);
        checkoutSessionsRequest.setLineItems(lineItemEntityDtoList);
        checkoutSessionsRequest.setPaymentMethodTypes(List.of("card"));
        checkoutSessionsRequest.setSubscriptionData(extractStripeSubscriptionData(trailDays));
        checkoutSessionsRequest.setCoupons(extractStripeCoupon(couponCode));
        log.info("CheckoutSessionsRequest : {}", checkoutSessionsRequest);
        return checkoutSessionsRequest;
    }


    private static StripeSubscriptionDataDto extractStripeSubscriptionData(Long trailDays) {
        if(ObjectUtils.isEmpty(trailDays) || trailDays == 0){
            return null;
        }
        StripeSubscriptionDataDto stripeSubscriptionDataDto = new StripeSubscriptionDataDto();
        stripeSubscriptionDataDto.setTrialPeriodDays(trailDays);
        return stripeSubscriptionDataDto;
    }

    private static List<StripeCouponDto> extractStripeCoupon(String couponCode){
        if(ObjectUtils.isEmpty(couponCode)){
            return new ArrayList<>();
        }
        List<StripeCouponDto> stripeCouponDtoList = new ArrayList<>();
        StripeCouponDto stripeCouponDto = createStripeCouponDto(couponCode);
        stripeCouponDtoList.add(stripeCouponDto);
        return stripeCouponDtoList;
    }

    @NotNull
    private static StripeCouponDto createStripeCouponDto(String couponCode) {
        StripeCouponDto stripeCouponDto = new StripeCouponDto();
        stripeCouponDto.setCouponId(couponCode);
        return stripeCouponDto;
    }

}
