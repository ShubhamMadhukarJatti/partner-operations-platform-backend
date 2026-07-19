package com.sharkdom.service.stripe;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.stripe.StripeMode;
import com.sharkdom.entity.stripe.*;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.mapper.stripe.LineItemMapper;
import com.sharkdom.mapper.stripe.StripeCheckoutSessionsMapper;
import com.sharkdom.mapper.stripe.StripeCouponMapper;
import com.sharkdom.mapper.stripe.StripeSubscriptionMapper;
import com.sharkdom.model.stripe.LineItemEntityDto;
import com.sharkdom.model.stripe.StripeCheckoutSessionsDto;
import com.sharkdom.model.stripe.StripeCouponDto;
import com.sharkdom.model.stripe.StripeSubscriptionDataDto;
import com.sharkdom.repository.stripe.StripeCheckoutRepository;
import com.sharkdom.repository.stripe.StripeCustomerRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeCheckoutServiceImpl implements StripeCheckoutService {

    private final StripeCheckoutRepository stripeCheckoutRepository;

    private final StripeCheckoutSessionsMapper stripeCheckoutSessionsMapper;

    private final StripeCustomerRepository stripeCustomerRepository;

    private final LineItemMapper lineItemMapper;

    private final StripeCouponMapper stripeCouponMapper;

    private final StripeSubscriptionMapper stripeSubscriptionMapper;

    private final StripeService stripeService;

    @Transactional
    @Override
    public StripeCheckoutSessionsDto createCheckoutSession(StripeCheckoutSessionsDto stripeCheckoutSessionsDto, @Valid boolean isBusinessCustomer) throws StripeException {
        try {
//            createStripeCheckoutSessionDtoRequest(stripeCheckoutSessionsDto);
            String customerId = stripeCheckoutSessionsDto.getCustomer().getCustomerId();
            String successUrl = stripeCheckoutSessionsDto.getSuccessUrl();
            String cancelUrl = stripeCheckoutSessionsDto.getCancelUrl();
            StripeMode stripeMode = stripeCheckoutSessionsDto.getMode();
            List<LineItemEntityDto> updatedLineItemEntityDto = stripeCheckoutSessionsDto.getLineItems();
            List<StripeCouponDto> stripeCouponDtoList = stripeCheckoutSessionsDto.getCoupons();
            StripeSubscriptionDataDto subscriptionDataDto = stripeCheckoutSessionsDto.getSubscriptionData();

            List<SessionCreateParams.LineItem> allLineItem = stripeService.createStripeLineItemsForSession(updatedLineItemEntityDto);
            List<SessionCreateParams.Discount> allDiscount = stripeService.createStripeDiscountsForSession(stripeCouponDtoList);

            Map<String, String> metadataCheckoutSessionCreation = Map.of(
                    "new_price_id", updatedLineItemEntityDto.get(0).getPrice().getStripePriceId(),
                    "action", "create"
            );

            SessionCreateParams.Builder params = getStripeSessionCreateParams(stripeCheckoutSessionsDto, allLineItem, allDiscount, metadataCheckoutSessionCreation);
            if (!ObjectUtils.isEmpty(stripeCouponDtoList)) {
                params.addAllDiscount(allDiscount);
            } else {
                params.setAllowPromotionCodes(true);
            }
            if(isBusinessCustomer) {
                getStripeSessionCreateParamsForBusinessCustomer(params);
            }
            Session checkoutSession = Session.create(params.build());
            log.info("Session Created: {}", checkoutSession.getId());

            StripeCheckoutSessionsDto updatedStripeCheckoutSessionsDto = getUpdatedStripeCheckoutSessionsDto(stripeCheckoutSessionsDto, checkoutSession);
            log.info("UpdatedStripeCheckoutSessionsDto: {}", updatedStripeCheckoutSessionsDto);
            StripeCheckoutSessions toSaveSession = stripeCheckoutSessionsMapper.stripeCheckoutSessionsDtoToStripeCheckoutSessions(updatedStripeCheckoutSessionsDto);

            List<LineItemEntity> lineItemEntities = lineItemMapper.lineItemEntityDtoListToLineItemEntityList(updatedStripeCheckoutSessionsDto.getLineItems());
            toSaveSession.setLineItems(lineItemEntities);

            List<StripeCoupon> stripeCoupons = stripeCouponMapper.stripeCouponDtoListToStripeCouponList(updatedStripeCheckoutSessionsDto.getCoupons());
            toSaveSession.setCoupons(stripeCoupons);

            StripeSubscriptionData subscriptionData = stripeSubscriptionMapper.stripeSubscriptionDataDtoToStripeSubscriptionData(updatedStripeCheckoutSessionsDto.getSubscriptionData());
            toSaveSession.setSubscriptionData(subscriptionData);

            StripeCustomer stripeCustomer = stripeCustomerRepository.findByCustomerId(checkoutSession.getCustomer()).orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH45));
            toSaveSession.setCustomer(stripeCustomer);
            log.info("To save Session : {}", toSaveSession);

            StripeCheckoutSessions savedStripeCheckoutSessions = stripeCheckoutRepository.save(toSaveSession);
            log.info("CheckoutSessions saved to database : {}", savedStripeCheckoutSessions);

            StripeCheckoutSessionsDto returnStripeCheckoutSessionsDto = stripeCheckoutSessionsMapper.stripeCheckoutSessionsToStripeCheckoutSessionsDto(savedStripeCheckoutSessions);
            returnStripeCheckoutSessionsDto.setCheckoutUrl(checkoutSession.getUrl());
            log.info("CheckoutSessionsDto saved to database: {}", returnStripeCheckoutSessionsDto);

            return returnStripeCheckoutSessionsDto;
        } catch (StripeException e) {
            log.error("Something went wrong from Stripe end: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Something went wrong while creating Checkout session: {}", e.getMessage());
            throw new ServiceException(ErrorMessages.SH150, "creating", e.getMessage());
        }
    }

    @Override
    @Transactional
    public StripeCheckoutSessionsDto getCheckoutSessionById(String checkoutSessionId) {
        StripeCheckoutSessions foundStripeCheckoutSessions = stripeCheckoutRepository.findBySessionId(checkoutSessionId)
                .orElseThrow(() -> new SharkdomException(ErrorMessages.SH128, checkoutSessionId));
        log.info("foundStripeCheckoutSessions : {}", foundStripeCheckoutSessions);
        StripeCheckoutSessionsDto stripeCheckoutSessionsDto = stripeCheckoutSessionsMapper.stripeCheckoutSessionsToStripeCheckoutSessionsDto(foundStripeCheckoutSessions);
        log.info("foundStripeCheckoutSessions DTO : {}", stripeCheckoutSessionsDto);
        return stripeCheckoutSessionsDto;
    }

    private SessionCreateParams.Builder getStripeSessionCreateParams(StripeCheckoutSessionsDto stripeCheckoutSessionsDto, List<SessionCreateParams.LineItem> allLineItem, List<SessionCreateParams.Discount> allDiscount, Map<String, String> metadataCheckoutSessionCreation) {
        return SessionCreateParams.builder()
                .addAllPaymentMethodType(stripeService.createAllPaymentMethodType(stripeCheckoutSessionsDto.getPaymentMethodTypes()))
                .setMode(stripeService.getStripeCheckoutMode(stripeCheckoutSessionsDto.getMode()))
                .setCustomer(stripeCheckoutSessionsDto.getCustomer().getCustomerId())
                .addAllLineItem(allLineItem)
                .setSubscriptionData(getSubscriptionDataByMode(stripeCheckoutSessionsDto).orElse(null))
                .setSuccessUrl(stripeCheckoutSessionsDto.getSuccessUrl())
                .setCancelUrl(stripeCheckoutSessionsDto.getCancelUrl())
                .setAutomaticTax(getSessionCreateAutomaticTax())
                .setCustomerUpdate(getSessionCreateUpdatedCustomerSetAddress().build())
                .putAllMetadata(metadataCheckoutSessionCreation);
    }

    private static SessionCreateParams.AutomaticTax getSessionCreateAutomaticTax() {
        return SessionCreateParams.AutomaticTax.builder()
                .setEnabled(true)  // Calculates tax based on customer location
                .build();
    }

    private static SessionCreateParams.CustomerUpdate.Builder getSessionCreateUpdatedCustomerSetAddress() {
        return SessionCreateParams.CustomerUpdate.builder()
                .setAddress(SessionCreateParams.CustomerUpdate.Address.AUTO);
    }

    private StripeCheckoutSessionsDto getUpdatedStripeCheckoutSessionsDto(StripeCheckoutSessionsDto stripeCheckoutSessionsDto, Session checkoutSession) {
        stripeCheckoutSessionsDto.setSessionId(checkoutSession.getId());
        stripeCheckoutSessionsDto.setExpiresAt(Instant.ofEpochSecond(checkoutSession.getExpiresAt())
                .atZone(ZoneId.systemDefault()).toLocalDate());
        StripeSubscriptionDataDto stripeSubscriptionDataDto;
        if (!ObjectUtils.isEmpty(checkoutSession.getSubscription())) {
            stripeSubscriptionDataDto = new StripeSubscriptionDataDto();
            stripeSubscriptionDataDto.setSubscriptionId(checkoutSession.getSubscription());
        }
        stripeCheckoutSessionsDto.setStatus(checkoutSession.getStatus());
        stripeCheckoutSessionsDto.setPaymentStatus(checkoutSession.getPaymentStatus());
        return stripeCheckoutSessionsDto;
    }

    private Optional<SessionCreateParams.SubscriptionData> getSubscriptionDataByMode(StripeCheckoutSessionsDto sessionsDto) {
        if (sessionsDto.getMode().equals(StripeMode.SUBSCRIPTION)) {
            return Optional.ofNullable(getSubscriptionDataParam(sessionsDto.getSubscriptionData()));
        }
        return Optional.empty();
    }

    private SessionCreateParams.SubscriptionData getSubscriptionDataParam(StripeSubscriptionDataDto subscriptionDataDto) {
        if (ObjectUtils.isEmpty(subscriptionDataDto)) {
            return null;
        }
        return SessionCreateParams.SubscriptionData.builder()
                .setTrialPeriodDays(subscriptionDataDto.getTrialPeriodDays())
                .build();
    }

    private static void getStripeSessionCreateParamsForBusinessCustomer(SessionCreateParams.Builder params) {
        params.setTaxIdCollection(getSessionCreateTaxIdCollection())
                .setCustomerUpdate(getSessionCreateUpdatedCustomerSetAddress()
                                .setName(SessionCreateParams.CustomerUpdate.Name.AUTO)
                                .build()
                );
    }

    private static SessionCreateParams.TaxIdCollection getSessionCreateTaxIdCollection() {
        return SessionCreateParams.TaxIdCollection.builder()
                .setEnabled(true)
                .build();
    }
}
