package com.personalweb.ai.controller;

import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personalweb.ai.dto.BatchIngestionRequest;
import com.personalweb.ai.service.KnowledgeIngestionService;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeIngestionService knowledgeIngestionService;

    public KnowledgeController(KnowledgeIngestionService knowledgeIngestionService) {
        this.knowledgeIngestionService = knowledgeIngestionService;
    }

    @PostMapping("/ingest/batch")
    public Map<String, Object> ingestBatch(@Validated @RequestBody BatchIngestionRequest request) {
        int chunkCount = knowledgeIngestionService.ingestBatch(request.getItems());
        return Map.of(
                "status", "ok",
                "itemCount", request.getItems().size(),
                "chunkCount", chunkCount);
    }
}
