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

    @Bean("cheapSharkRestClient")
    public RestClient cheapSharkRestClient(@Value("${cheapshark.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean("hltbRestClient")
    public RestClient hltbRestClient(@Value("${hltb.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                .defaultHeader("Origin", "https://howlongtobeat.com")
                .defaultHeader("Referer", "https://howlongtobeat.com/")
                .build();
    }
}
