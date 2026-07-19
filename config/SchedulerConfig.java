package com.sharkdom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    @Bean
    @Primary
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5); // Number of threads in the pool
        scheduler.setThreadNamePrefix("ScheduledTask-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true); // Graceful shutdown
        scheduler.setAwaitTerminationSeconds(1000); // Wait for tasks to complete
        return scheduler;
    }
}
