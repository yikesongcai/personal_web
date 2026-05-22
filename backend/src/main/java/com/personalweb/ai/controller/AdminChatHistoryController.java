package com.personalweb.ai.controller;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import com.personalweb.ai.dto.ChatHistoryTurnResponse;
import com.personalweb.ai.service.ChatHistoryService;

@RestController
@RequestMapping("/api/admin/chat-history")
public class AdminChatHistoryController {

    private final JdbcTemplate jdbcTemplate;
    private final ChatHistoryService chatHistoryService;

    public AdminChatHistoryController(JdbcTemplate jdbcTemplate, ChatHistoryService chatHistoryService) {
        this.jdbcTemplate = jdbcTemplate;
        this.chatHistoryService = chatHistoryService;
    }

    @GetMapping("/sessions")
    public Map<String, Object> listSessions(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        int offset = (page - 1) * size;
        String countSql = "SELECT COUNT(DISTINCT session_id) FROM chat_turn";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class);
        if (total == null) total = 0L;

        String sql = """
            SELECT t.session_id,
                   (SELECT user_question FROM chat_turn WHERE session_id = t.session_id ORDER BY id ASC LIMIT 1) AS first_question,
                   COUNT(*) AS turn_count,
                   MIN(t.created_at) AS first_created_at,
                   MAX(t.created_at) AS last_created_at
            FROM chat_turn t
            GROUP BY t.session_id
            ORDER BY MAX(t.created_at) DESC
            LIMIT ? OFFSET ?
            """;
        List<Map<String, Object>> sessions = jdbcTemplate.queryForList(sql, size, offset);

        return Map.of("data", sessions, "total", total, "page", page, "size", size);
    }

    @GetMapping("/sessions/{sessionId}")
    public List<ChatHistoryTurnResponse> sessionDetail(@PathVariable String sessionId) {
        return chatHistoryService.listBySessionId(sessionId);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Map<String, String> deleteSession(@PathVariable String sessionId) {
        jdbcTemplate.update("DELETE FROM chat_turn WHERE session_id = ?", sessionId);
        return Map.of("ok", "deleted");
    }
}
