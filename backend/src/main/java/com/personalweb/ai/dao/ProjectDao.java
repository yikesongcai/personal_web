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
        project.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null) {
            project.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return project;
    };

    public List<Project> findAll() {
        return jdbcTemplate.query("SELECT * FROM project ORDER BY id DESC", rowMapper);
    }

    public Project findById(Long id) {
        List<Project> list = jdbcTemplate.query("SELECT * FROM project WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long insert(Project project) {
        jdbcTemplate.update(
            "INSERT INTO project (title, cover_image, frameworks, online_url, github_url, summary, content) VALUES (?, ?, ?, ?, ?, ?, ?)",
            project.getTitle(), project.getCoverImage(), project.getFrameworks(), project.getOnlineUrl(), project.getGithubUrl(), project.getSummary(), project.getContent()
        );
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void update(Project project) {
        jdbcTemplate.update(
            "UPDATE project SET title=?, cover_image=?, frameworks=?, online_url=?, github_url=?, summary=?, content=? WHERE id=?",
            project.getTitle(), project.getCoverImage(), project.getFrameworks(), project.getOnlineUrl(), project.getGithubUrl(), project.getSummary(), project.getContent(), project.getId()
        );
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM project WHERE id = ?", id);
    }
}
