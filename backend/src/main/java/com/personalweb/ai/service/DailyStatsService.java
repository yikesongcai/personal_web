package com.personalweb.ai.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DailyStatsService {

    private final JdbcTemplate jdbcTemplate;
    private final ConcurrentHashMap<String, Long> visitCache = new ConcurrentHashMap<>();

    public DailyStatsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Add token consumption for today (atomic upsert). */
    public void addTokens(int tokens) {
        if (tokens <= 0) return;
        try {
            jdbcTemplate.update(
                "INSERT INTO daily_stats (record_date, tokens, visits) VALUES (CURDATE(), ?, 0) " +
                "ON DUPLICATE KEY UPDATE tokens = tokens + ?",
                tokens, tokens
            );
        } catch (Exception e) {
            System.err.println("[DailyStats] Failed to record tokens: " + e.getMessage());
        }
    }

    /** Add a visit count for today, with IP deduplication (30-min window). */
    public void addVisit(String ip) {
        if (ip == null || ip.isBlank()) return;
        long now = System.currentTimeMillis();
        Long last = visitCache.putIfAbsent(ip, now);
        if (last != null && (now - last) < TimeUnit.MINUTES.toMillis(30)) {
            return; // dedup: same IP within 30 min
        }
        // refresh timestamp and count
        visitCache.put(ip, now);
        try {
            jdbcTemplate.update(
                "INSERT INTO daily_stats (record_date, tokens, visits) VALUES (CURDATE(), 0, 1) " +
                "ON DUPLICATE KEY UPDATE visits = visits + 1"
            );
        } catch (Exception e) {
            System.err.println("[DailyStats] Failed to record visit: " + e.getMessage());
        }
    }
}
