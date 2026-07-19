package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.entity.OutreachAutomationSettings;
import com.sharkdom.agenticai.model.OutreachAutomationRequest;
import com.sharkdom.agenticai.model.OutreachAutomationResponse;
import com.sharkdom.agenticai.repository.OutreachAutomationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutreachAutomationService {

    private final OutreachAutomationRepository repository;

    public OutreachAutomationResponse saveSettings(
            OutreachAutomationRequest request,
            Long orgId,
            String userId) {

        OutreachAutomationSettings settings =
                repository.findByOrgId(orgId)
                        .orElse(new OutreachAutomationSettings());

        settings.setOrgId(orgId);
        settings.setUserId(userId);
        settings.setDailyFrequency(request.getDailyFrequency());
        settings.setSearchStrictness(request.getSearchStrictness());
        settings.setLinkedinActive(request.getLinkedinActive());
        settings.setEmailActive(request.getEmailActive());

        repository.save(settings);

        OutreachAutomationResponse response =
                new OutreachAutomationResponse();

        response.setDailyFrequency(settings.getDailyFrequency());
        response.setSearchStrictness(settings.getSearchStrictness());
        response.setLinkedinActive(settings.getLinkedinActive());
        response.setEmailActive(settings.getEmailActive());

        return response;
    }

}
