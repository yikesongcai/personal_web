package com.personalweb.ai.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DailyStatsService {

    private final JdbcTemplate jdbcTemplate;

    public DailyStatsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Add token consumption for today.
     * Uses INSERT ... ON DUPLICATE KEY UPDATE for atomicity.
     */
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

    /**
     * Add a visit count for today.
     */
    public void addVisit() {
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
