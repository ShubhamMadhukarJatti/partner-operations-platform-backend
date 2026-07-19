package com.sharkdom.mapper.stripe;

import com.sharkdom.constants.stripe.StripePlanType;
import com.sharkdom.entity.stripe.StripePlanConfiguration;
import com.sharkdom.model.stripe.StripePlanConfigurationRequest;
import com.sharkdom.model.stripe.StripePlanConfigurationResponse;

public class StripePlanConfigurationMapper {

    private StripePlanConfigurationMapper() {
    }

    public static StripePlanConfigurationResponse toResponse(StripePlanConfiguration stripePlanConfiguration) {
        StripePlanConfigurationResponse stripePlanConfigurationResponse = new StripePlanConfigurationResponse();
        stripePlanConfigurationResponse.setPlanType(stripePlanConfiguration.getPlanType().name());
        stripePlanConfigurationResponse.setAmount(stripePlanConfiguration.getAmount());
        stripePlanConfigurationResponse.setAiProposalCredits(stripePlanConfiguration.getAiProposalCredits());
        stripePlanConfigurationResponse.setCollaborationSent(stripePlanConfiguration.getCollaborationSent());
        stripePlanConfigurationResponse.setPlaygroundCredits(stripePlanConfiguration.getPlaygroundCredits());
        stripePlanConfigurationResponse.setPriceId(stripePlanConfiguration.getPriceId());
        stripePlanConfigurationResponse.setCurrency(stripePlanConfiguration.getCurrency());
        stripePlanConfigurationResponse.setSeat(stripePlanConfiguration.getSeat());
        return stripePlanConfigurationResponse;
    }

    public static StripePlanConfiguration toEntity(StripePlanType stripePlanType, StripePlanConfigurationRequest stripePlanConfigurationRequest) {
        StripePlanConfiguration stripePlanConfiguration = new StripePlanConfiguration();
        stripePlanConfiguration.setPlanType(stripePlanType);
        stripePlanConfiguration.setAiProposalCredits(stripePlanConfigurationRequest.getAiProposalCredits());
        stripePlanConfiguration.setCollaborationSent(stripePlanConfigurationRequest.getCollaborationSent());
        stripePlanConfiguration.setPlaygroundCredits(stripePlanConfigurationRequest.getPlaygroundCredits());
        stripePlanConfiguration.setPriceId(stripePlanConfigurationRequest.getPriceId());
        stripePlanConfiguration.setSeat(stripePlanConfigurationRequest.getSeat());
        return stripePlanConfiguration;
    }

}
