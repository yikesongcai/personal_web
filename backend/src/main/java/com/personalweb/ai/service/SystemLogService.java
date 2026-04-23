package com.personalweb.ai.service;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SystemLogService {

    private final JdbcTemplate jdbcTemplate;

    public SystemLogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(String level, String module, String message) {
        try {
            jdbcTemplate.update(
                "INSERT INTO system_log (level, module, message) VALUES (?, ?, ?)",
                level, module, message
            );
        } catch (Exception e) {
            System.err.println("[SystemLog] Failed to write log: " + e.getMessage());
        }
    }

    public void info(String module, String message) {
        log("INFO", module, message);
    }

    public void warn(String module, String message) {
        log("WARN", module, message);
    }

    public void error(String module, String message) {
        log("ERROR", module, message);
    }

    public List<Map<String, Object>> getLogs(int page, int size, String level) {
        String sql;
        if (level != null && !level.isBlank() && !"ALL".equalsIgnoreCase(level)) {
            sql = "SELECT * FROM system_log WHERE level = ? ORDER BY id DESC LIMIT ? OFFSET ?";
            return jdbcTemplate.queryForList(sql, level, size, (page - 1) * size);
        } else {
            sql = "SELECT * FROM system_log ORDER BY id DESC LIMIT ? OFFSET ?";
            return jdbcTemplate.queryForList(sql, size, (page - 1) * size);
        }
    }

    public int countLogs(String level) {
        if (level != null && !level.isBlank() && !"ALL".equalsIgnoreCase(level)) {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM system_log WHERE level = ?", Integer.class, level);
            return count != null ? count : 0;
        } else {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM system_log", Integer.class);
            return count != null ? count : 0;
        }
    }

    public void clearLogs() {
        jdbcTemplate.update("DELETE FROM system_log");
    }
}
