package com.lifelog.diary.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClientConfig {

    @Value("${fast-api.base-url}")
    private String fastApiBaseUrl;

    @Bean
    public org.springframework.web.reactive.function.client.WebClient webClient() {
        return org.springframework.web.reactive.function.client.WebClient.builder()
                .baseUrl(fastApiBaseUrl)
                .build();
    }
}
