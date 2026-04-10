package com.personalweb.ai.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.personalweb.ai.entity.Article;

@Repository
public class ArticleDao {

    private final JdbcTemplate jdbcTemplate;

    public ArticleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Article> rowMapper = (ResultSet rs, int rowNum) -> {
        Article article = new Article();
        article.setId(rs.getLong("id"));
        article.setTitle(rs.getString("title"));
        article.setSummary(rs.getString("summary"));
        article.setContent(rs.getString("content"));
        article.setTags(rs.getString("tags"));
        article.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null) {
            article.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return article;
    };

    public List<Article> findAll() {
        return jdbcTemplate.query("SELECT * FROM article ORDER BY id DESC", rowMapper);
    }

    public Article findById(Long id) {
        List<Article> list = jdbcTemplate.query("SELECT * FROM article WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long insert(Article article) {
        jdbcTemplate.update(
            "INSERT INTO article (title, summary, content, tags) VALUES (?, ?, ?, ?)",
            article.getTitle(), article.getSummary(), article.getContent(), article.getTags()
        );
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void update(Article article) {
        jdbcTemplate.update(
            "UPDATE article SET title=?, summary=?, content=?, tags=? WHERE id=?",
            article.getTitle(), article.getSummary(), article.getContent(), article.getTags(), article.getId()
        );
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM article WHERE id = ?", id);
    }
}
