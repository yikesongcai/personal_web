package com.personalweb.ai.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personalweb.ai.service.RateLimitWhitelistService;

@RestController
@RequestMapping("/api/admin/rate-limit/whitelist")
public class AdminRateLimitController {

    private final RateLimitWhitelistService whitelistService;

    public AdminRateLimitController(RateLimitWhitelistService whitelistService) {
        this.whitelistService = whitelistService;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return whitelistService.getWhitelist();
    }

    @PostMapping
    public Map<String, String> add(@RequestBody Map<String, String> payload) {
        String ip = payload.get("ip");
        String remark = payload.get("remark");
        if (ip == null || ip.isBlank()) {
            throw new IllegalArgumentException("IP address is required");
        }
        whitelistService.addIp(ip, remark);
        return Map.of("status", "ok");
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        whitelistService.deleteIp(id);
        return Map.of("status", "ok");
    }
}
