package com.sharkdom.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {

        // Create request factory with the custom HttpClient
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();

        // Configure timeouts (in milliseconds)
        factory.setConnectTimeout(10000);  // 10 seconds
//        factory.setReadTimeout(30000);     // 30 seconds
        factory.setReadTimeout(120000);// 2 minutes
        return new RestTemplate(factory);
    }
}