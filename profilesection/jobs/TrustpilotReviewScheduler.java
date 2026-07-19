package com.sharkdom.profilesection.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrustpilotReviewScheduler {

    private final TrustpilotReviewSyncService syncService;

    @Scheduled(cron = "0 0 0 */4 * *")
    public void runReviewSync() {

        log.info("Starting scheduled Trustpilot sync");

        syncService.syncReviews();

    }
}