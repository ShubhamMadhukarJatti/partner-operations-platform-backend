package com.sharkdom.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(AppProperties.class)
@Configuration
@RequiredArgsConstructor
@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default Server URL")})
@EnableCaching
public class AppConfig {

    private final AppProperties appProperties;

}

