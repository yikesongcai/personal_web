package com.personalweb.ai.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(JdbcTemplate jdbcTemplate) {
        return args -> {
            // Check if projects exist
            Integer projectCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM project", Integer.class);
            if (projectCount != null && projectCount == 0) {
                jdbcTemplate.update("INSERT INTO project (title, frameworks, content) VALUES " +
                        "('E-Commerce Analytics', 'Spring Boot / Vue', '# E-Commerce Analytics\\nContent goes here.')," +
                        "('AI Code Assistant', 'Python / React', '# AI Code Assistant\\nFeature details.')," +
                        "('Smart Home Hub', 'Node.js / IoT', '# Smart Home Hub\\nArchitecture overview.')");
                System.out.println("Inserted mock projects.");
            }

            // Check if articles exist
            Integer articleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM article", Integer.class);
            if (articleCount != null && articleCount == 0) {
                jdbcTemplate.update("INSERT INTO article (title, tags, content) VALUES " +
                        "('Vue 3 Composition API Guide', 'Frontend', '# Vue 3 Guide\\nDetailed explanation...')," +
                        "('Spring Boot Microservices', 'Backend', '# Microservices\\nArchitecture analysis...')," +
                        "('Getting Started with ECharts', 'Tutorial', '# ECharts Basics\\nCharting with Vue...')," +
                        "('Optimizing Java Garbage Collection', 'Performance', '# GC Tuning\\nTips and tricks.')");
                System.out.println("Inserted mock articles.");
            }

            // Check if daily_stats exist
            Integer statsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM daily_stats", Integer.class);
            if (statsCount != null && statsCount == 0) {
                jdbcTemplate.update("INSERT INTO daily_stats (record_date, tokens, visits) VALUES " +
                        "('2026-04-01', 12500, 120)," +
                        "('2026-04-02', 15000, 145)," +
                        "('2026-04-03', 11000, 110)," +
                        "('2026-04-04', 18000, 190)," +
                        "('2026-04-05', 22000, 210)," +
                        "('2026-04-06', 17500, 160)," +
                        "('2026-04-07', 16000, 150)," +
                        "('2026-04-08', 21000, 230)," +
                        "('2026-04-09', 19000, 180)," +
                        "('2026-04-10', 8500,  95)");
                System.out.println("Inserted mock daily_stats.");
            }
            // Check if rate_limit_whitelist exist
            Integer whitelistCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM rate_limit_whitelist", Integer.class);
            if (whitelistCount != null && whitelistCount == 0) {
                jdbcTemplate.update("INSERT INTO rate_limit_whitelist (ip, remark) VALUES " +
                        "('127.0.0.1', 'Local IPv4')," +
                        "('0:0:0:0:0:0:0:1', 'Local IPv6')");
                System.out.println("Inserted mock rate_limit_whitelist.");
            }
        };
    }
}
