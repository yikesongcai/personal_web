package com.personalweb.ai.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.personalweb.ai.service.SystemLogService;

@RestController
@RequestMapping("/api/admin/logs")
public class AdminSystemLogController {

    private final SystemLogService systemLogService;

    public AdminSystemLogController(SystemLogService systemLogService) {
        this.systemLogService = systemLogService;
    }

    @GetMapping
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "ALL") String level) {
        List<Map<String, Object>> logs = systemLogService.getLogs(page, size, level);
        int total = systemLogService.countLogs(level);
        return Map.of("data", logs, "total", total, "page", page, "size", size);
    }

    @DeleteMapping
    public Map<String, String> clear() {
        systemLogService.clearLogs();
        systemLogService.info("SYSTEM", "管理员清空了系统日志");
        return Map.of("status", "ok");
    }
}
