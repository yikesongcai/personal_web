package com.personalweb.ai.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personalweb.ai.dto.DashVectorDebugCleanupRequest;
import com.personalweb.ai.dto.DashVectorRawDebugRequest;
import com.personalweb.ai.service.DashVectorRawDebugService;

@RestController
@RequestMapping("/api/debug/dashvector")
public class DashVectorDebugController {

    private final DashVectorRawDebugService rawDebugService;

    public DashVectorDebugController(DashVectorRawDebugService rawDebugService) {
        this.rawDebugService = rawDebugService;
    }

    @PostMapping("/raw-once")
    public Map<String, Object> rawOnce(@RequestBody(required = false) DashVectorRawDebugRequest request) {
        DashVectorRawDebugRequest payload = request == null ? new DashVectorRawDebugRequest() : request;
        return rawDebugService.rawDebugOnce(payload);
    }

    @PostMapping("/cleanup-debug-docs")
    public Map<String, Object> cleanupDebugDocs(@RequestBody(required = false) DashVectorDebugCleanupRequest request) {
        DashVectorDebugCleanupRequest payload = request == null ? new DashVectorDebugCleanupRequest() : request;
        return rawDebugService.cleanupDebugDocs(payload);
    }
}
