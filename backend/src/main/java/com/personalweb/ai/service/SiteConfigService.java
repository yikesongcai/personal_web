package com.personalweb.ai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SiteConfigService {

    private final JdbcTemplate jdbcTemplate;

    public SiteConfigService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, String> getAll() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT config_key, config_value FROM site_config");
        Map<String, String> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            result.put((String) row.get("config_key"), (String) row.get("config_value"));
        }
        return result;
    }

    public void set(String key, String value) {
        jdbcTemplate.update(
            "INSERT INTO site_config (config_key, config_value) VALUES (?, ?) " +
            "ON DUPLICATE KEY UPDATE config_value = ?",
            key, value, value
        );
    }

    public void batchSet(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }
}
