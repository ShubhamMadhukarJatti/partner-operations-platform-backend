package com.sharkdom.service.ai;

import com.sharkdom.model.ai.OverlapFrequency;
import com.sharkdom.model.ai.OverlapRequest;
import com.sharkdom.model.ai.PersonaMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManualOverlapService {

    private final OverlapVersioningService versioningService;
    private final java.util.List<OverlapDataProvider> providers;

    public void runManualOverlap(Long orgId, PersonaMode persona) {

        log.info("Manual overlap execution started. orgId={} persona={}", orgId, persona);

        OverlapDataProvider provider = providers.stream()
                .filter(p -> p.getSource() == persona)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Provider not found for persona: " + persona));

        log.info("Fetching data from provider={}", persona);

        OverlapRequest request = provider.fetch(orgId);

        if (request == null || request.getFields() == null || request.getFields().isEmpty()) {

            log.warn("No data returned from provider. orgId={} persona={}", orgId, persona);
            return;
        }

        // Frequency doesn't matter in manual run
        request.setFrequency(OverlapFrequency.NONE);

        log.info("Running versioning manually. orgId={} persona={} fields={}",
                orgId,
                persona,
                request.getFields().size());

        versioningService.runVersioning(request);

        log.info("Manual overlap execution completed. orgId={} persona={}", orgId, persona);
    }
}