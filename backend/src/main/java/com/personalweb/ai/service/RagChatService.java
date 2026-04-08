package com.personalweb.ai.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalweb.ai.config.RagProperties;
import com.personalweb.ai.dto.ChatRequest;
import com.personalweb.ai.dto.SourceReference;
import com.personalweb.ai.exception.BusinessException;

import reactor.core.publisher.Flux;

@Service
public class RagChatService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final RagProperties ragProperties;
        private final ChatHistoryService chatHistoryService;
        private final ObjectMapper objectMapper;

        public RagChatService(
                VectorStore vectorStore,
                ChatClient.Builder chatClientBuilder,
                RagProperties ragProperties,
                ChatHistoryService chatHistoryService,
                ObjectMapper objectMapper) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
        this.ragProperties = ragProperties;
        this.chatHistoryService = chatHistoryService;
        this.objectMapper = objectMapper;
    }

        public Flux<ServerSentEvent<String>> chat(ChatRequest requestPayload) {
        String question = requestPayload.getQuestion();
            if (!StringUtils.hasText(question)) {
                throw BusinessException.badRequest("QUESTION_EMPTY", "question 不能为空");
            }

        String sessionId = StringUtils.hasText(requestPayload.getSessionId())
                    ? requestPayload.getSessionId()
                    : UUID.randomUUID().toString();

        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(ragProperties.getTopK())
                .similarityThreshold(ragProperties.getSimilarityThreshold())
                .build();

        List<Document> docs = vectorStore.similaritySearch(request);
        String context = buildContext(docs);
        List<SourceReference> sources = buildSources(docs);
        String sourcesJson = toSourcesJson(sources);

        String systemPrompt = "你是一个专业的个人网站智能导览助手。请严格基于以下提供的网站内容回答用户问题。"
                + "如果上下文没有相关信息，请明确说明无法从知识库中找到答案。\n\n"
                + "内容:\n" + context;

        StringBuilder answerBuilder = new StringBuilder();

        Flux<ServerSentEvent<String>> sourcesEvent = Flux.just(
                ServerSentEvent.<String>builder()
                        .event("sources")
                        .data(sourcesJson)
                        .build());

        Flux<ServerSentEvent<String>> tokenEventStream = chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .stream()
                .content()
                .doOnNext(answerBuilder::append)
                .map(delta -> ServerSentEvent.<String>builder()
                        .event("token")
                        .data(delta)
                        .build())
                .doOnComplete(() -> chatHistoryService.saveTurn(
                        sessionId,
                        question,
                        answerBuilder.toString(),
                        sourcesJson));

        Flux<ServerSentEvent<String>> doneEvent = Flux.just(
                ServerSentEvent.<String>builder()
                        .event("done")
                        .data("[DONE]")
                        .build());

        return sourcesEvent
                .concatWith(tokenEventStream)
                .concatWith(doneEvent)
                .onErrorResume(ex -> {
                    chatHistoryService.saveTurn(sessionId, question, "", sourcesJson);
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data("回答生成失败，请稍后重试。")
                            .build());
                });
    }

    public List<SourceReference> searchSources(String question) {
        if (!StringUtils.hasText(question)) {
            throw BusinessException.badRequest("QUESTION_EMPTY", "question 不能为空");
        }

        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(ragProperties.getTopK())
                .similarityThreshold(ragProperties.getSimilarityThreshold())
                .build();

        List<Document> docs = vectorStore.similaritySearch(request);
        return buildSources(docs);
    }

    private String buildContext(List<Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return "[空上下文]";
        }

        return docs.stream()
                .map(doc -> {
                    String source = String.valueOf(doc.getMetadata().getOrDefault("url", "unknown"));
                    return "来源: " + source + "\n" + doc.getText();
                })
                .collect(Collectors.joining("\n\n----\n\n"));
    }

    private List<SourceReference> buildSources(List<Document> docs) {
        Map<String, SourceReference> uniqueSources = new LinkedHashMap<>();

        for (Document doc : docs) {
            Map<String, Object> metadata = doc.getMetadata();
            String url = String.valueOf(metadata.getOrDefault("url", "unknown"));
            String sourceType = String.valueOf(metadata.getOrDefault("sourceType", "unknown"));
            String title = String.valueOf(metadata.getOrDefault("title", metadata.getOrDefault("projectName", "未命名来源")));

            String key = url + "|" + title;
            uniqueSources.putIfAbsent(key, new SourceReference(title, url, sourceType));
        }

        return List.copyOf(uniqueSources.values());
    }

    private String toSourcesJson(List<SourceReference> sources) {
        try {
            return objectMapper.writeValueAsString(sources);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }
}
