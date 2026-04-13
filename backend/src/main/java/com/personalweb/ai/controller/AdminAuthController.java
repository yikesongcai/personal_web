package com.personalweb.ai.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    @GetMapping("/verify")
    public Map<String, Object> verify() {
        return Map.of("status", "ok", "message", "Authentication successful");
    }
}
