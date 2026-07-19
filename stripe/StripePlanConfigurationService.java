package com.sharkdom.service.stripe;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.stripe.StripePlanType;
import com.sharkdom.entity.stripe.StripePlanConfiguration;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.mapper.stripe.StripePlanConfigurationMapper;
import com.sharkdom.model.stripe.StripePlanConfigurationRequest;
import com.sharkdom.model.stripe.StripePlanConfigurationResponse;
import com.sharkdom.repository.stripe.StripePlanConfigurationRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePlanConfigurationService {

    private final StripePlanConfigurationRepository stripePlanConfigurationRepository;

    @Transactional
    public StripePlanConfigurationResponse addStripePlanConfiguration(StripePlanType stripePlanType, StripePlanConfigurationRequest stripePlanConfigurationRequest) throws StripeException {
        try {
            StripePlanConfiguration stripePlanConfigurationMapperEntity = StripePlanConfigurationMapper.toEntity(stripePlanType, stripePlanConfigurationRequest);
            mapCurrencyAndAmount(stripePlanConfigurationRequest, stripePlanConfigurationMapperEntity);
            StripePlanConfiguration saved = stripePlanConfigurationRepository.save(stripePlanConfigurationMapperEntity);
            return StripePlanConfigurationMapper.toResponse(saved);
        } catch (StripeException e) {
            log.error("Something went wrong while receiving price info from stripe. Error {}", stripePlanType, e);
            throw e;
        } catch (Exception e) {
            log.error("Something went wrong while adding {}", stripePlanType, e);
            throw new ServiceException(ErrorMessages.SH148, "adding", stripePlanType, e.getMessage());
        }
    }

    private static void mapCurrencyAndAmount(StripePlanConfigurationRequest stripePlanConfigurationRequest, StripePlanConfiguration stripePlanConfigurationMapperEntity) throws StripeException {
        Price retrievedPrice = Price.retrieve(stripePlanConfigurationRequest.getPriceId());
        stripePlanConfigurationMapperEntity.setCurrency(retrievedPrice.getCurrency());
        stripePlanConfigurationMapperEntity.setAmount((double) retrievedPrice.getUnitAmount() / 100);
    }

    @Transactional
    public StripePlanConfigurationResponse updateStripePlanConfiguration(StripePlanType stripePlanType, StripePlanConfigurationRequest stripePlanConfigurationRequest) throws StripeException {
        try {
            StripePlanConfiguration foundStripePlanConfiguration = stripePlanConfigurationRepository.findById(stripePlanType)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH53, stripePlanType));
            foundStripePlanConfiguration.setAiProposalCredits(!ObjectUtils.isEmpty(stripePlanConfigurationRequest.getAiProposalCredits()) ? stripePlanConfigurationRequest.getAiProposalCredits() : foundStripePlanConfiguration.getAiProposalCredits());
            foundStripePlanConfiguration.setPlaygroundCredits(!ObjectUtils.isEmpty(stripePlanConfigurationRequest.getPlaygroundCredits()) ? stripePlanConfigurationRequest.getPlaygroundCredits() : foundStripePlanConfiguration.getPlaygroundCredits());
            foundStripePlanConfiguration.setCollaborationSent(!ObjectUtils.isEmpty(stripePlanConfigurationRequest.getCollaborationSent()) ? stripePlanConfigurationRequest.getCollaborationSent() : foundStripePlanConfiguration.getCollaborationSent());
            foundStripePlanConfiguration.setPriceId(!ObjectUtils.isEmpty(stripePlanConfigurationRequest.getPriceId()) ? stripePlanConfigurationRequest.getPriceId() : foundStripePlanConfiguration.getPriceId());
            mapCurrencyAndAmount(stripePlanConfigurationRequest, foundStripePlanConfiguration);
            foundStripePlanConfiguration.setSeat(stripePlanConfigurationRequest.getSeat());
            StripePlanConfiguration saved = stripePlanConfigurationRepository.save(foundStripePlanConfiguration);
            return StripePlanConfigurationMapper.toResponse(saved);
        } catch (StripeException e) {
            log.error("Something went wrong while receiving price info from stripe. Error {}", stripePlanType, e);
            throw e;
        } catch (Exception e) {
            log.error("Something went wrong while updating {}", stripePlanType, e);
            throw new ServiceException(ErrorMessages.SH148, "updating", stripePlanType, e.getMessage());
        }
    }

    @Transactional
    public StripePlanConfigurationResponse getStripePlanConfiguration(StripePlanType stripePlanType) {
        try {
            StripePlanConfiguration foundStripePlanConfiguration = stripePlanConfigurationRepository.findById(stripePlanType)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH53, stripePlanType));
            return StripePlanConfigurationMapper.toResponse(foundStripePlanConfiguration);
        } catch (Exception e) {
            log.error("Something went wrong while receiving configuration from database of plan type {}", stripePlanType.name(), e);
            throw new ServiceException(ErrorMessages.SH148, "retrieving", stripePlanType, e.getMessage());
        }
    }

    @Transactional
    public String getPriceIdByPlanType(StripePlanType stripePlanType) {
        try {
            return stripePlanConfigurationRepository.findPriceIdByPlanType(stripePlanType);
        } catch (Exception e) {
            log.error("Something went wrong while receiving Price Id from database of plan type {}", stripePlanType, e);
            throw new ServiceException(ErrorMessages.SH147, stripePlanType, e.getMessage());
        }
    }

    @Transactional
    public StripePlanConfigurationResponse updatePriceIdByPlanType(StripePlanType stripePlanType, String priceId) throws StripeException {
        try {
            StripePlanConfiguration foundStripePlanConfiguration = stripePlanConfigurationRepository.findById(stripePlanType)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH53 ,stripePlanType));
            Price retrievedPrice = Price.retrieve(priceId);
            foundStripePlanConfiguration.setCurrency(retrievedPrice.getCurrency());
            foundStripePlanConfiguration.setAmount((double) retrievedPrice.getUnitAmount() / 100);
            foundStripePlanConfiguration.setPriceId(!ObjectUtils.isEmpty(priceId) ? priceId : foundStripePlanConfiguration.getPriceId());
            StripePlanConfiguration saved = stripePlanConfigurationRepository.save(foundStripePlanConfiguration);
            return StripePlanConfigurationMapper.toResponse(saved);
        } catch (StripeException e) {
            log.error("Something went wrong while receiving price info from stripe. Error {}", stripePlanType, e);
            throw e;
        } catch (Exception e) {
            log.error("Something went wrong while updating {}", stripePlanType, e);
            throw new ServiceException(ErrorMessages.SH146, stripePlanType, e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StripePlanType getPlanTypeByPriceId(String priceId) {
        return stripePlanConfigurationRepository.findPlanTypeByPriceId(priceId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH54, priceId));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long getSeatByPlanType(StripePlanType stripePlanType){
        StripePlanConfiguration foundStripePlanConfiguration = stripePlanConfigurationRepository.findById(stripePlanType)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH53 ,stripePlanType));
        return foundStripePlanConfiguration.getSeat();
    }

    @Transactional
    public List<StripePlanConfigurationResponse> getAllStripePlanConfiguration() {
        try {
            return stripePlanConfigurationRepository.findAll().stream()
                    .map(StripePlanConfigurationMapper::toResponse)
                    .toList();
        } catch (Exception e){
            log.error("Something went wrong while fetching stripe configuration. Error: {}", e.getMessage());
            throw new ServiceException(ErrorMessages.SH116, e.getMessage());
        }
    }

//    public List<String> getProductIdsByPlanType(StripePlanType planType) {
//
//        switch (planType) {
//
//            case BASIC:
//                return List.of("prod_partner_onboarding");
//
//            case STANDARD:
//                return List.of(
//                        "prod_partner_onboarding",
//                        "prod_partner_management"
//                );
//
//            case PREMIUM:
//                return List.of(
//                        "prod_partner_onboarding",
//                        "prod_partner_management",
//                        "prod_deal_registration"
//                );
//
//            default:
//                throw new RuntimeException("Invalid plan");
//        }
//    }
}
