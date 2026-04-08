package com.personalweb.ai.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalweb.ai.dto.ChatHistoryTurnResponse;
import com.personalweb.ai.dto.SourceReference;

@Service
public class ChatHistoryService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ChatHistoryService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public void saveTurn(String sessionId, String question, String answer, String sourcesJson) {
        jdbcTemplate.update(
                "INSERT INTO chat_turn(session_id, user_question, assistant_answer, sources_json) VALUES (?, ?, ?, ?)",
                sessionId,
                question,
                answer,
                sourcesJson);
    }

    public List<ChatHistoryTurnResponse> listBySessionId(String sessionId) {
        return jdbcTemplate.query(
                "SELECT session_id, user_question, assistant_answer, sources_json, created_at FROM chat_turn WHERE session_id = ? ORDER BY id ASC",
                (rs, rowNum) -> {
                    ChatHistoryTurnResponse turn = new ChatHistoryTurnResponse();
                    turn.setSessionId(rs.getString("session_id"));
                    turn.setQuestion(rs.getString("user_question"));
                    turn.setAnswer(rs.getString("assistant_answer"));
                    turn.setSources(parseSources(rs.getString("sources_json")));

                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        turn.setCreatedAt(createdAt.toLocalDateTime());
                    } else {
                        turn.setCreatedAt(LocalDateTime.now());
                    }
                    return turn;
                },
                sessionId);
    }

    private List<SourceReference> parseSources(String sourcesJson) {
        if (sourcesJson == null || sourcesJson.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(sourcesJson, new TypeReference<List<SourceReference>>() {
            });
        } catch (IOException ex) {
            return Collections.emptyList();
        }
    }
}
