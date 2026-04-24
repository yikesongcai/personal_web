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
            // ---------- Schema migrations (safe, idempotent) ----------
            // Add summary column to project if it doesn't exist yet (compatible with older MySQL)
            try {
                Integer summaryExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'project' AND COLUMN_NAME = 'summary'",
                    Integer.class
                );
                if (summaryExists == null || summaryExists == 0) {
                    jdbcTemplate.execute("ALTER TABLE project ADD COLUMN summary VARCHAR(500) NULL AFTER github_url");
                    System.out.println("[Migration] Added summary column to project table.");
                }
            } catch (Exception e) {
                System.err.println("[Migration] summary column migration failed: " + e.getMessage());
            }
            // Create system_log table if missing
            try {
                jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS system_log (" +
                    "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "  level VARCHAR(16) NOT NULL DEFAULT 'INFO'," +
                    "  module VARCHAR(64) NOT NULL," +
                    "  message TEXT NOT NULL," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
                );
            } catch (Exception e) {
                System.err.println("[Migration] system_log table: " + e.getMessage());
            }
            // Add sort_order, is_featured, deleted to project
            migrateColumn(jdbcTemplate, "project", "sort_order", "ALTER TABLE project ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER summary");
            migrateColumn(jdbcTemplate, "project", "is_featured", "ALTER TABLE project ADD COLUMN is_featured TINYINT(1) NOT NULL DEFAULT 0 AFTER sort_order");
            migrateColumn(jdbcTemplate, "project", "deleted", "ALTER TABLE project ADD COLUMN deleted TINYINT(1) NOT NULL DEFAULT 0 AFTER is_featured");
            // Add sort_order, is_featured, deleted to article
            migrateColumn(jdbcTemplate, "article", "sort_order", "ALTER TABLE article ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER tags");
            migrateColumn(jdbcTemplate, "article", "is_featured", "ALTER TABLE article ADD COLUMN is_featured TINYINT(1) NOT NULL DEFAULT 0 AFTER sort_order");
            migrateColumn(jdbcTemplate, "article", "deleted", "ALTER TABLE article ADD COLUMN deleted TINYINT(1) NOT NULL DEFAULT 0 AFTER is_featured");
            // Create site_config table
            try {
                jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS site_config (" +
                    "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "  config_key VARCHAR(64) NOT NULL UNIQUE," +
                    "  config_value TEXT," +
                    "  description VARCHAR(255)," +
                    "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
                );
                // Seed default site_config values
                String[] keys = {"hero_kicker","hero_title","hero_subtitle","projects_section_title","projects_section_subtitle","articles_section_title","articles_section_subtitle"};
                String[] vals = {"SONG'S LAB","Personal Web Portfolio","聚合项目作品、技术文章与研究笔记，并通过 RAG 助手实现自然语言导览。","项目精选","围绕 AI 系统、联邦学习安全与工程效率的长期实践。","最新文章","记录系统设计、工程复盘与模型落地经验。"};
                for (int i = 0; i < keys.length; i++) {
                    jdbcTemplate.update(
                        "INSERT INTO site_config (config_key, config_value) VALUES (?, ?) ON DUPLICATE KEY UPDATE config_key = config_key",
                        keys[i], vals[i]);
                }
            } catch (Exception e) {
                System.err.println("[Migration] site_config table: " + e.getMessage());
            }
            // ----------------------------------------------------------
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

    /** Check if a column exists in a table; run the ALTER only if missing */
    private static void migrateColumn(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate,
                                      String table, String column, String ddl) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class, table, column
            );
            if (count == null || count == 0) {
                jdbcTemplate.execute(ddl);
                System.out.println("[Migration] Added column " + column + " to " + table);
            }
        } catch (Exception e) {
            System.err.println("[Migration] " + table + "." + column + ": " + e.getMessage());
        }
    }
}
