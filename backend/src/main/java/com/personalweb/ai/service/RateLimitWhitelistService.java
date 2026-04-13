package com.personalweb.ai.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class RateLimitWhitelistService {

    private final JdbcTemplate jdbcTemplate;
    private final Set<String> whitelistCache = ConcurrentHashMap.newKeySet();

    public RateLimitWhitelistService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        refreshCache();
    }

    @Scheduled(fixedRate = 60000)
    public void refreshCache() {
        try {
            List<String> list = jdbcTemplate.queryForList("SELECT ip FROM rate_limit_whitelist", String.class);
            whitelistCache.clear();
            whitelistCache.addAll(list);
            System.out.println("Rate limit whitelist refreshed: " + whitelistCache.size() + " items");
        } catch (Exception e) {
            System.err.println("Failed to refresh rate limit whitelist: " + e.getMessage());
        }
    }

    public boolean isWhitelisted(String ip) {
        return ip != null && whitelistCache.contains(ip);
    }
    
    public List<Map<String, Object>> getWhitelist() {
        return jdbcTemplate.queryForList("SELECT * FROM rate_limit_whitelist ORDER BY id DESC");
    }
    
    public void addIp(String ip, String remark) {
        jdbcTemplate.update("INSERT INTO rate_limit_whitelist (ip, remark) VALUES (?, ?)", ip, remark);
        refreshCache();
    }
    
    public void deleteIp(Long id) {
        jdbcTemplate.update("DELETE FROM rate_limit_whitelist WHERE id = ?", id);
        refreshCache();
    }
}
