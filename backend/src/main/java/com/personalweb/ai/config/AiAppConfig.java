package com.personalweb.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(RagProperties.class)
@EnableScheduling
public class AiAppConfig {
}
