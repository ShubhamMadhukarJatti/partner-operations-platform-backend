package com.sharkdom.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "")
@Data
@Slf4j
public class AppProperties {

    private Map<String, String> emailEventToTemplateMap;

    public String getEmailTemplateCodeForEvent(String event) {
        if (!emailEventToTemplateMap.containsKey(event)) {
            log.error("Email template name not found for event " + event);
            throw new RuntimeException("Email Template name not found for subscription event " + event);
        }
        return emailEventToTemplateMap.get(event);
    }

}
