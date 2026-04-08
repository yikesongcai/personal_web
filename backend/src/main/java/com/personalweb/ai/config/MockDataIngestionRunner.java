package com.personalweb.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.personalweb.ai.service.KnowledgeIngestionService;

@Configuration
public class MockDataIngestionRunner {

    private static final Logger log = LoggerFactory.getLogger(MockDataIngestionRunner.class);

    @Value("${app.rag.auto-ingest-mock:false}")
    private boolean autoIngestMock;

    @Bean
    CommandLineRunner runMockIngestion(KnowledgeIngestionService ingestionService) {
        return args -> {
            if (!autoIngestMock) {
                log.info("已禁用启动自动 Mock 入库（app.rag.auto-ingest-mock=false）");
                return;
            }

            try {
                ingestionService.ingestMockProject();
            } catch (Exception ex) {
                log.warn("启动时 Mock 入库失败，已跳过，不影响服务启动: {}", ex.getMessage());
            }
        };
    }
}
