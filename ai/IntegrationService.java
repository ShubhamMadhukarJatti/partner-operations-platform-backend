package com.sharkdom.service.ai;

import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.user.SlackIntegrationRepository;
import com.sharkdom.util.Util;
import org.springframework.stereotype.Service;

@Service
public class IntegrationService {
    private final IntegrationRepository integrationRepository;
    private final SlackIntegrationRepository slackIntegrationRepository;

    public IntegrationService(IntegrationRepository integrationRepository, SlackIntegrationRepository slackIntegrationRepository) {
        this.integrationRepository = integrationRepository;
        this.slackIntegrationRepository = slackIntegrationRepository;
    }

    public void deleteIntegration(String userId, IntegrationType integrationType) {
        if (IntegrationType.SLACK.equals(integrationType)) {
            slackIntegrationRepository.deleteByUserId(userId);
            integrationRepository.deleteByUserIdAndIntegrationType(userId, integrationType);

        } else {
            integrationRepository.deleteByUserIdAndIntegrationType(userId, integrationType);
        }
    }
}
