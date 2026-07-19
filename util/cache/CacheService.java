package com.sharkdom.util.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CacheService {

    @Autowired
    CacheManager cacheManager;

    @Scheduled(fixedRate = 3600000)
    public void refreshAllCaches() {
        log.info("Refreshing cache...");
        cacheManager.getCacheNames().stream()
                .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

}