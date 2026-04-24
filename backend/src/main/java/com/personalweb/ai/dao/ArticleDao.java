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
        article.setSortOrder(rs.getInt("sort_order"));
        article.setIsFeatured(rs.getBoolean("is_featured"));
        article.setDeleted(rs.getBoolean("deleted"));
        article.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null) {
            article.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return article;
    };

    public List<Article> findAll() {
        return jdbcTemplate.query(
            "SELECT * FROM article WHERE deleted = 0 ORDER BY sort_order ASC, id DESC",
            rowMapper);
    }

    public List<Article> findFeatured() {
        return jdbcTemplate.query(
            "SELECT * FROM article WHERE deleted = 0 AND is_featured = 1 ORDER BY sort_order ASC, id DESC",
            rowMapper);
    }

    public Article findById(Long id) {
        List<Article> list = jdbcTemplate.query(
            "SELECT * FROM article WHERE id = ? AND deleted = 0", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long insert(Article article) {
        jdbcTemplate.update(
            "INSERT INTO article (title, summary, content, tags, sort_order, is_featured) VALUES (?, ?, ?, ?, ?, ?)",
            article.getTitle(), article.getSummary(), article.getContent(), article.getTags(),
            article.getSortOrder() != null ? article.getSortOrder() : 0,
            article.getIsFeatured() != null && article.getIsFeatured() ? 1 : 0
        );
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void update(Article article) {
        jdbcTemplate.update(
            "UPDATE article SET title=?, summary=?, content=?, tags=?, sort_order=?, is_featured=? WHERE id=?",
            article.getTitle(), article.getSummary(), article.getContent(), article.getTags(),
            article.getSortOrder() != null ? article.getSortOrder() : 0,
            article.getIsFeatured() != null && article.getIsFeatured() ? 1 : 0,
            article.getId()
        );
    }

    /** Soft delete */
    public void delete(Long id) {
        jdbcTemplate.update("UPDATE article SET deleted = 1 WHERE id = ?", id);
    }
}
