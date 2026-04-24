package com.personalweb.ai.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.personalweb.ai.entity.Project;

@Repository
public class ProjectDao {

    private final JdbcTemplate jdbcTemplate;

    public ProjectDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Project> rowMapper = (ResultSet rs, int rowNum) -> {
        Project project = new Project();
        project.setId(rs.getLong("id"));
        project.setTitle(rs.getString("title"));
        project.setCoverImage(rs.getString("cover_image"));
        project.setFrameworks(rs.getString("frameworks"));
        project.setOnlineUrl(rs.getString("online_url"));
        project.setGithubUrl(rs.getString("github_url"));
        project.setSummary(rs.getString("summary"));
        project.setContent(rs.getString("content"));
        project.setSortOrder(rs.getInt("sort_order"));
        project.setIsFeatured(rs.getBoolean("is_featured"));
        project.setDeleted(rs.getBoolean("deleted"));
        project.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null) {
            project.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return project;
    };

    public List<Project> findAll() {
        return jdbcTemplate.query(
            "SELECT * FROM project WHERE deleted = 0 ORDER BY sort_order ASC, id DESC",
            rowMapper);
    }

    public List<Project> findFeatured() {
        return jdbcTemplate.query(
            "SELECT * FROM project WHERE deleted = 0 AND is_featured = 1 ORDER BY sort_order ASC, id DESC",
            rowMapper);
    }

    public Project findById(Long id) {
        List<Project> list = jdbcTemplate.query(
            "SELECT * FROM project WHERE id = ? AND deleted = 0", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long insert(Project project) {
        jdbcTemplate.update(
            "INSERT INTO project (title, cover_image, frameworks, online_url, github_url, summary, content, sort_order, is_featured) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            project.getTitle(), project.getCoverImage(), project.getFrameworks(),
            project.getOnlineUrl(), project.getGithubUrl(), project.getSummary(),
            project.getContent(),
            project.getSortOrder() != null ? project.getSortOrder() : 0,
            project.getIsFeatured() != null && project.getIsFeatured() ? 1 : 0
        );
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void update(Project project) {
        jdbcTemplate.update(
            "UPDATE project SET title=?, cover_image=?, frameworks=?, online_url=?, github_url=?, summary=?, content=?, sort_order=?, is_featured=? WHERE id=?",
            project.getTitle(), project.getCoverImage(), project.getFrameworks(),
            project.getOnlineUrl(), project.getGithubUrl(), project.getSummary(),
            project.getContent(),
            project.getSortOrder() != null ? project.getSortOrder() : 0,
            project.getIsFeatured() != null && project.getIsFeatured() ? 1 : 0,
            project.getId()
        );
    }

    /** Soft delete */
    public void delete(Long id) {
        jdbcTemplate.update("UPDATE project SET deleted = 1 WHERE id = ?", id);
    }
}
