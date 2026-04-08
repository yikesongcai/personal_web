package com.personalweb.ai.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.personalweb.ai.service.KnowledgeIngestionService;

@Configuration
public class MockDataIngestionRunner {

    @Bean
    CommandLineRunner runMockIngestion(KnowledgeIngestionService ingestionService) {
        return args -> ingestionService.ingestMockProject();
    }
}
