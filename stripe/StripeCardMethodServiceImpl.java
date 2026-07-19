package com.sharkdom.service.stripe;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.dto.SetupIntentResponse;
import com.sharkdom.entity.stripe.StripeCardDetail;
import com.sharkdom.entity.stripe.StripeCustomer;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.mapper.stripe.StripeCardDetailsMapper;
import com.sharkdom.model.stripe.StripeCardDetailDto;
import com.sharkdom.repository.stripe.StripeCardDetailRepository;
import com.sharkdom.repository.stripe.StripeCustomerRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;
import com.stripe.param.SetupIntentCreateParams;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StripeCardMethodServiceImpl implements StripeCardMethodService {

    @Resource
    private StripeCardMethodServiceImpl stripeCardMethodService;

    private final StripeService stripeService;

    private final StripeCustomerRepository stripeCustomerRepository;
    private final StripeCardDetailsMapper stripeCardDetailsMapper;
    private final StripeCardDetailRepository stripeCardDetailRepository;

    @Override
    @Transactional
    public StripeCardDetailDto getAndSaveCustomerCardDetails(String customerId) throws StripeException {
        PaymentMethod.Card card = stripeService.getCustomerPaymentMethod(customerId);
        StripeCustomer stripeCustomer = stripeCustomerRepository.findByCustomerId(customerId).orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH44, customerId));
        StripeCardDetail cardDetail = getCardDetails(card);
        cardDetail.setCustomer(stripeCustomer);
        stripeCustomer.addCard(cardDetail);
        cardDetail.setStatus("primary");
        StripeCardDetail savedStripeCardDetail = stripeCardDetailRepository.save(cardDetail);
        List<StripeCardDetail> cardDetails = stripeCardDetailRepository.findAll();
        List<StripeCardDetail> secondaryCarDetailList = cardDetails.stream()
                .filter(cardInfo -> !Objects.equals(cardInfo.getId(), savedStripeCardDetail.getId()))
                .toList();
        for(StripeCardDetail cardInfo : secondaryCarDetailList){
            cardInfo.setStatus("secondary");
        }
        stripeCardDetailRepository.saveAll(secondaryCarDetailList);
        return stripeCardDetailsMapper.stripeCardDetailToStripeCardDetailDto(savedStripeCardDetail);
    }

    @Override
    @Transactional
    public StripeCardDetailDto updatePaymentMethod(String customerId, String paymentMethodId) throws StripeException {
        Customer customer = stripeService.updateCustomerDefaultPaymentMethod(customerId, paymentMethodId);
        return stripeCardMethodService.getAndSaveCustomerCardDetails(customer.getId());
    }

//    @Override
//    public StripeCardDetailDto getSubscriptionCardDetails(String subscriptionId) throws StripeException {
//        PaymentMethod.Card card = stripeService.getSubscriptionPaymentMethod(subscriptionId);
//        return getCardDetails(card);
//    }

    @NotNull
    private static StripeCardDetail getCardDetails(PaymentMethod.Card card) {
        return StripeCardDetail.builder()
                .brand(card.getBrand())
                .last4(card.getLast4())
                .expMonth(card.getExpMonth())
                .expYear(card.getExpYear())
                .country(card.getCountry()).build();
    }

    public SetupIntentResponse createSetupIntent(String customerId) throws StripeException {

        SetupIntentCreateParams params =
                SetupIntentCreateParams.builder()
                        .setCustomer(customerId)
                        .addPaymentMethodType("card")
                        .build();

        SetupIntent setupIntent = SetupIntent.create(params);

        return new SetupIntentResponse(
                setupIntent.getId(),
                setupIntent.getClientSecret()
        );
    }
}

