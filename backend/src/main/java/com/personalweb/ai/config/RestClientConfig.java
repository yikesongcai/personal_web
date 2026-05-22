package com.personalweb.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * 提供 RestClient.Builder Bean，使 Spring AI Chroma (基于 RestClient)
 * 能在 WebFlux 项目中使用。
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
