package com.sharkdom.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "csm.alert")
public class CSMAlertProperties {
    private String email;
}