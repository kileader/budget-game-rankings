package com.kevinleader.bgr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean("igdbRestClient")
    public RestClient igdbRestClient(@Value("${igdb.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean("twitchRestClient")
    public RestClient twitchRestClient() {
        return RestClient.create();
    }
}
