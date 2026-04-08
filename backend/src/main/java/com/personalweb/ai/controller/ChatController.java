package com.personalweb.ai.controller;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personalweb.ai.dto.ChatRequest;
import com.personalweb.ai.dto.ChatHistoryTurnResponse;
import com.personalweb.ai.service.ChatHistoryService;
import com.personalweb.ai.service.RagChatService;

import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final RagChatService ragChatService;
    private final ChatHistoryService chatHistoryService;

    public ChatController(RagChatService ragChatService, ChatHistoryService chatHistoryService) {
        this.ragChatService = ragChatService;
        this.chatHistoryService = chatHistoryService;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@Validated @RequestBody ChatRequest request) {
        return ragChatService.chat(request);
    }

    @GetMapping("/chat/history/{sessionId}")
    public List<ChatHistoryTurnResponse> history(@PathVariable String sessionId) {
        return chatHistoryService.listBySessionId(sessionId);
    }
}
