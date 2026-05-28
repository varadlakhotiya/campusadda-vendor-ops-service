package com.campusadda.vendorops.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MlServiceConfig {

    @Bean
    public RestTemplate mlRestTemplate() {
        return new RestTemplate();
    }
}
