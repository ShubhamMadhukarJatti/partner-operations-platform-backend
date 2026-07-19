package com.sharkdom.partnertraining.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GcpConfig {

    @Bean
    public Storage storage() {
        return StorageOptions.getDefaultInstance().getService();
    }

    @Bean
    public Tika tika() {
        return new Tika();
    }
}

