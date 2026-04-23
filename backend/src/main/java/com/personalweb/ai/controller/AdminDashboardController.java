package com.personalweb.ai.controller;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final JdbcTemplate jdbcTemplate;

    public AdminDashboardController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        Integer projectCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM project", Integer.class);
        Integer articleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM article", Integer.class);
        Integer chatCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM chat_turn", Integer.class);
        Map<String, Object> tokenSum = jdbcTemplate.queryForMap(
            "SELECT COALESCE(SUM(tokens), 0) AS total_tokens, COALESCE(SUM(visits), 0) AS total_visits FROM daily_stats"
        );
        return Map.of(
            "projectCount", projectCount != null ? projectCount : 0,
            "articleCount", articleCount != null ? articleCount : 0,
            "chatCount", chatCount != null ? chatCount : 0,
            "totalTokens", tokenSum.get("total_tokens"),
            "totalVisits", tokenSum.get("total_visits")
        );
    }

    @GetMapping("/stats")
    public Map<String, Object> stats(@RequestParam(defaultValue = "day") String period) {
        String sql;
        List<Map<String, Object>> rows;
        if ("year".equals(period)) {
            sql = "SELECT DATE_FORMAT(record_date, '%Y') AS label, SUM(tokens) AS tokens, SUM(visits) AS visits " +
                  "FROM daily_stats GROUP BY DATE_FORMAT(record_date, '%Y') ORDER BY label ASC LIMIT 10";
        } else if ("month".equals(period)) {
            sql = "SELECT DATE_FORMAT(record_date, '%Y-%m') AS label, SUM(tokens) AS tokens, SUM(visits) AS visits " +
                  "FROM daily_stats WHERE record_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
                  "GROUP BY DATE_FORMAT(record_date, '%Y-%m') ORDER BY label ASC";
        } else {
            // day: last 30 days
            sql = "SELECT DATE_FORMAT(record_date, '%m-%d') AS label, tokens, visits " +
                  "FROM daily_stats WHERE record_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) ORDER BY record_date ASC";
        }
        rows = jdbcTemplate.queryForList(sql);
        return Map.of("data", rows, "period", period);
    }
}
