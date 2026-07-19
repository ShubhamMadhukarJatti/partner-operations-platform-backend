/*
package com.sharkdom.util.aws.config;

import com.sharkdom.entity.configuration.Configuration;
import com.sharkdom.service.configuration.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommonConfigProvider {


    final static String configurationCacheName = "configurations";
    @Autowired
    ConfigurationService configurationService;

    @Autowired
    CacheManager cacheManager;

    @Scheduled(fixedRate = 600000)
    public void refresh() {
        cacheManager.getCache(configurationCacheName).clear();
    }

    @Cacheable(configurationCacheName)
    public List<Configuration> getAllConfigurations() {
        return configurationService.findAllActive();
    }

    public Configuration getByTypeAndKey(String type, String key) {
        try {
            return getAllConfigurations().stream()
                    .filter(c -> c.getType().equals(type) && c.getKey().equals(key)).findAny().get();
        } catch (NullPointerException e) {
            String errorMessage = "Cannot find configuration for type " + type + " and key " + key;
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }

    }

    public List<Configuration> getAllByType(String type) {
        return getAllConfigurations().stream()
                .filter(c -> c.getType().equals(type)).collect(Collectors.toList());
    }

}
*/
