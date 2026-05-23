package com.trip_ai.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient wahaWebClient(
            @Value("${tripai.waha.url}") String url,
            @Value("${tripai.waha.api-key}") String apiKey) {
        return WebClient.builder()
            .baseUrl(url)
            .defaultHeader("X-Api-Key", apiKey)
            .build();
    }

    @Bean
    WebClient travelpayoutsWebClient(@Value("${tripai.travelpayouts.base-url}") String url) {
        return WebClient.builder()
            .baseUrl(url)
            .build();
    }
}
