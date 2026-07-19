package com.sharkdom.agenticai.confignew;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@ConfigurationProperties(prefix = "dweep.agent")
public class DweepAgentConfig {

    private String baseUrl = "https://sharkdom-dweep-agent-g8hhhrhhcghjdmb9.canadacentral-01.azurewebsites.net";

    
}