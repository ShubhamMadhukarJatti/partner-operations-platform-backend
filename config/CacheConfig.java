package com.sharkdom.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sharkdom.partnerattribution.dto.SalesforceDealData;
import com.sharkdom.salesforce.dto.SalesforceTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {


    @Value("${otp.expiry.minutes}")
    private long OTP_EXPIRY_TIME;

    @Bean
    public Cache<String, String> otpCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(OTP_EXPIRY_TIME, TimeUnit.MINUTES) // OTP expires after OTP_EXPIRY_TIME minutes
                .maximumSize(1000) // Maximum number of OTPs to store
                .build();
    }

    @Bean
    public Cache<String, Boolean> emailSentCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(10000)
                .recordStats()
                .build();
    }

    @Bean
    public Cache<String, SalesforceTokenResponse> salesforceTokenCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(45, TimeUnit.MINUTES)
                .maximumSize(500)
                .recordStats()
                .build();
    }

    @Bean
    public Cache<String, String> salesforceWebsiteCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10000)
                .recordStats()
                .build();
    }

    @Bean
    public Cache<String, SalesforceDealData> salesforceDealDataCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10000)
                .recordStats()
                .build();
    }

    @Bean
    public Cache<String, String> zohoAccessTokenCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(55, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats()
                .build();
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService sharedAccountsExecutor() {
        return Executors.newFixedThreadPool(8);
    }
}
