package com.e_commerce.e_commerce.configuration;

import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GhnClientConfig {
    @Bean
    WebClient ghnWebClient(
            @Value("${ghn.base-url}") String baseUrl,
            @Value("${ghn.token}") String token,
            @Value("${ghn.shop-id}") String shopId
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Token", token)
                .defaultHeader("ShopId", shopId)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
