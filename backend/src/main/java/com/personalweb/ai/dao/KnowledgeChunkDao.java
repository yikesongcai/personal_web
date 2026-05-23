package com.personalweb.ai.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.personalweb.ai.entity.KnowledgeChunk;

@Repository
public class KnowledgeChunkDao {

    private final JdbcTemplate jdbcTemplate;

    public KnowledgeChunkDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<KnowledgeChunk> rowMapper = (ResultSet rs, int rowNum) -> {
        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setId(rs.getLong("id"));
        chunk.setDocId(rs.getString("doc_id"));
        chunk.setDocType(rs.getString("doc_type"));
        chunk.setTitle(rs.getString("title"));
        chunk.setContent(rs.getString("content"));
        chunk.setChunkIndex(rs.getInt("chunk_index"));
        chunk.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return chunk;
    };

    public List<KnowledgeChunk> findAll(int page, int size) {
        int offset = (page - 1) * size;
        return jdbcTemplate.query(
            "SELECT * FROM knowledge_chunk ORDER BY id DESC LIMIT ? OFFSET ?",
            rowMapper, size, offset);
    }

    public KnowledgeChunk findById(Long id) {
        List<KnowledgeChunk> list = jdbcTemplate.query(
            "SELECT * FROM knowledge_chunk WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<KnowledgeChunk> findByDocId(String docId) {
        return jdbcTemplate.query(
            "SELECT * FROM knowledge_chunk WHERE doc_id = ? ORDER BY chunk_index ASC",
            rowMapper, docId);
    }

    public List<KnowledgeChunk> findForTitleSearch(int limit) {
        return jdbcTemplate.query(
            "SELECT * FROM knowledge_chunk WHERE title IS NOT NULL ORDER BY id DESC LIMIT ?",
            rowMapper, limit);
    }

    public void insert(KnowledgeChunk chunk) {
        jdbcTemplate.update(
            "INSERT INTO knowledge_chunk (doc_id, doc_type, title, content, chunk_index) VALUES (?, ?, ?, ?, ?)",
            chunk.getDocId(), chunk.getDocType(), chunk.getTitle(),
            chunk.getContent(), chunk.getChunkIndex());
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM knowledge_chunk WHERE id = ?", id);
    }

    public void deleteByDocId(String docId) {
        jdbcTemplate.update("DELETE FROM knowledge_chunk WHERE doc_id = ?", docId);
    }

    public long count() {
        Long result = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM knowledge_chunk", Long.class);
        return result != null ? result : 0;
    }

    public long countByDocId(String docId) {
        Long result = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM knowledge_chunk WHERE doc_id = ?", Long.class, docId);
        return result != null ? result : 0;
    }
}
