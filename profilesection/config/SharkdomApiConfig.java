package com.sharkdom.profilesection.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "evaluate.api")
public class SharkdomApiConfig {

    private String url;
    private String token;

}