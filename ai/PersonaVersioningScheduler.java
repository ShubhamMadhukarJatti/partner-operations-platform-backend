package com.sharkdom.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonaVersioningScheduler {

    private final PersonaVersioningService service;

    @Scheduled(fixedDelay = 600000)
    public void run() {

        log.info("PERSONA SCHEDULER TRIGGERED {}", new Date());

        service.runScheduler();
    }

    @Scheduled(fixedDelay = 600000)
    public void runScheduler() {

        log.info("PERSONA SCHEDULER TRIGGERED {}", new Date());

//        service.runSchedulerForSalesforce();
    }
}
