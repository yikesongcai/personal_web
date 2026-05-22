package com.personalweb.ai.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import com.personalweb.ai.config.RagProperties;

@RestController
@RequestMapping("/api/admin/ai-config")
public class AdminAIConfigController {

    @Value("${spring.ai.openai.chat.options.model}")
    private String chatModel;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.embedding.options.model}")
    private String embeddingModel;

    @Value("${spring.ai.openai.chat.options.temperature:0.2}")
    private double temperature;

    private final RagProperties ragProperties;

    public AdminAIConfigController(RagProperties ragProperties) {
        this.ragProperties = ragProperties;
    }

    @GetMapping
    public Map<String, Object> getConfig() {
        return Map.of(
            "chatModel", chatModel,
            "baseUrl", baseUrl,
            "embeddingModel", embeddingModel,
            "temperature", temperature,
            "ragTopK", ragProperties.getTopK(),
            "ragSimilarityThreshold", ragProperties.getSimilarityThreshold()
        );
    }
}
