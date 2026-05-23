package com.personalweb.ai.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalweb.ai.config.RagProperties;
import com.personalweb.ai.dao.KnowledgeChunkDao;
import com.personalweb.ai.dto.ChatRequest;
import com.personalweb.ai.dto.SourceReference;
import com.personalweb.ai.entity.KnowledgeChunk;
import com.personalweb.ai.exception.BusinessException;

import reactor.core.publisher.Flux;

@Service
public class RagChatService {

    private static final Logger log = LoggerFactory.getLogger(RagChatService.class);
    private static final int TITLE_FALLBACK_LIMIT = 500;

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final RagProperties ragProperties;
    private final ChatHistoryService chatHistoryService;
    private final KnowledgeChunkDao knowledgeChunkDao;
    private final ObjectMapper objectMapper;
    private final DailyStatsService dailyStatsService;

    public RagChatService(
            VectorStore vectorStore,
            ChatClient.Builder chatClientBuilder,
            RagProperties ragProperties,
            ChatHistoryService chatHistoryService,
            KnowledgeChunkDao knowledgeChunkDao,
            ObjectMapper objectMapper,
            DailyStatsService dailyStatsService) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
        this.ragProperties = ragProperties;
        this.chatHistoryService = chatHistoryService;
        this.knowledgeChunkDao = knowledgeChunkDao;
        this.objectMapper = objectMapper;
        this.dailyStatsService = dailyStatsService;
    }

    public Flux<ServerSentEvent<String>> chat(ChatRequest requestPayload) {
        String question = requestPayload.getQuestion();
        if (!StringUtils.hasText(question)) {
            throw BusinessException.badRequest("QUESTION_EMPTY", "question 不能为空");
        }

        String sessionId = StringUtils.hasText(requestPayload.getSessionId())
                ? requestPayload.getSessionId()
                : UUID.randomUUID().toString();

        return Flux.defer(() -> buildChatStream(question, sessionId))
                .onErrorResume(ex -> handleChatError(sessionId, question, ex));
    }

    private Flux<ServerSentEvent<String>> buildChatStream(String question, String sessionId) {
        List<Document> docs = searchDocuments(question);
        String context = buildContext(docs);
        List<SourceReference> sources = buildSources(docs);
        String sourcesJson = toSourcesJson(sources);

        String systemPrompt = "你是一个专业的个人网站智能导览助手。请严格基于以下提供的网站内容回答用户问题。"
                + "如果上下文没有相关信息，请明确说明无法从知识库中找到答案。"
                + "不要编造上下文中没有的项目、文章、技术细节或结论；如果信息不足，请说明需要补充哪些信息。"
                + "如果上下文已经命中了用户提到的项目或文章，但用户问到的某个功能在该项目中不存在或没有资料，"
                + "请先说明已找到该项目，再基于上下文概括它真实包含的功能，并明确指出没有找到该功能的资料。\n\n"
                + "内容:\n" + context;

        StringBuilder answerBuilder = new StringBuilder();

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
                .doOnComplete(() -> recordSuccessfulTurn(sessionId, question, answerBuilder.toString(), sourcesJson,
                        systemPrompt));

        Flux<ServerSentEvent<String>> sourcesEvent = Flux.just(
                ServerSentEvent.<String>builder()
                        .event("sources")
                        .data(sourcesJson)
                        .build());

        Flux<ServerSentEvent<String>> doneEvent = Flux.just(
                ServerSentEvent.<String>builder()
                        .event("done")
                        .data("[DONE]")
                        .build());

        return tokenEventStream
                .concatWith(sourcesEvent)
                .concatWith(doneEvent);
    }

    private List<Document> searchDocuments(String question) {
        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(ragProperties.getTopK())
                .similarityThreshold(ragProperties.getSimilarityThreshold())
                .build();

        try {
            List<Document> docs = vectorStore.similaritySearch(request);
            return mergeTitleMatches(question, preferDirectTitleMatches(question, docs));
        } catch (Exception ex) {
            List<Document> titleMatches = findTitleMatchesFromMirror(question);
            if (!titleMatches.isEmpty()) {
                log.warn("Vector search failed, using title fallback from knowledge mirror, question={}", question, ex);
                return titleMatches;
            }
            throw new ChatFlowException("知识库检索失败，请检查 RAG 数据库连接后重试。", ex);
        }
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

        try {
            List<Document> docs = vectorStore.similaritySearch(request);
            return buildSources(mergeTitleMatches(question, preferDirectTitleMatches(question, docs)));
        } catch (Exception ex) {
            List<Document> titleMatches = findTitleMatchesFromMirror(question);
            if (!titleMatches.isEmpty()) {
                log.warn("Source vector search failed, using title fallback from knowledge mirror, question={}", question, ex);
                return buildSources(titleMatches);
            }
            log.error("Source search failed, question={}", question, ex);
            throw BusinessException.externalService("RAG_SEARCH_FAILED", "知识库检索失败，请检查 RAG 数据库连接后重试。");
        }
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
        if (docs == null || docs.isEmpty()) {
            return List.of();
        }

        Map<String, SourceReference> uniqueSources = new LinkedHashMap<>();

        for (Document doc : docs) {
            Map<String, Object> metadata = doc.getMetadata();
            String url = String.valueOf(metadata.getOrDefault("url", "unknown"));
            String sourceType = String.valueOf(metadata.getOrDefault("sourceType", "unknown"));
            String title = String.valueOf(metadata.getOrDefault("title", metadata.getOrDefault("projectName", "未命名来源")));

            if (!StringUtils.hasText(url) || "unknown".equalsIgnoreCase(url)) {
                continue;
            }

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

    private List<Document> preferDirectTitleMatches(String question, List<Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return List.of();
        }

        String normalizedQuestion = normalizeForMatching(question);
        List<Document> directMatches = docs.stream()
                .filter(doc -> titleMatchesQuestion(normalizedQuestion, doc))
                .toList();

        return directMatches.isEmpty() ? docs : directMatches;
    }

    private List<Document> mergeTitleMatches(String question, List<Document> docs) {
        List<Document> titleMatches = findTitleMatchesFromMirror(question);
        if (titleMatches.isEmpty()) {
            return docs == null ? List.of() : docs;
        }

        Map<String, Document> merged = new LinkedHashMap<>();
        titleMatches.forEach(doc -> merged.put(documentKey(doc), doc));
        if (docs != null) {
            docs.forEach(doc -> merged.putIfAbsent(documentKey(doc), doc));
        }
        return List.copyOf(merged.values());
    }

    private List<Document> findTitleMatchesFromMirror(String question) {
        String normalizedQuestion = normalizeForMatching(question);
        try {
            return knowledgeChunkDao.findForTitleSearch(TITLE_FALLBACK_LIMIT).stream()
                    .filter(chunk -> titleMatchesQuestion(normalizedQuestion, chunk.getTitle()))
                    .map(this::toDocument)
                    .toList();
        } catch (Exception ex) {
            log.warn("Title fallback search failed, question={}", question, ex);
            return List.of();
        }
    }

    private Document toDocument(KnowledgeChunk chunk) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("sourceType", chunk.getDocType());
        metadata.put("title", chunk.getTitle());
        metadata.put("url", toSourceUrl(chunk));

        return new Document(chunk.getDocId(), chunk.getContent(), metadata);
    }

    private String toSourceUrl(KnowledgeChunk chunk) {
        if (chunk.getDocId() == null) {
            return "unknown";
        }

        if (chunk.getDocId().startsWith("project_")) {
            return "/projects/" + chunk.getDocId().substring("project_".length());
        }
        if (chunk.getDocId().startsWith("article_")) {
            return "/articles/" + chunk.getDocId().substring("article_".length());
        }
        return "/knowledge/" + chunk.getDocId();
    }

    private String documentKey(Document doc) {
        Object id = doc.getId();
        if (id != null) {
            return String.valueOf(id);
        }
        Map<String, Object> metadata = doc.getMetadata();
        return metadata.getOrDefault("url", "") + "|" + metadata.getOrDefault("title", "");
    }

    private boolean titleMatchesQuestion(String normalizedQuestion, Document doc) {
        Map<String, Object> metadata = doc.getMetadata();
        return titleMatchesQuestion(normalizedQuestion, metadata.get("title"))
                || titleMatchesQuestion(normalizedQuestion, metadata.get("projectName"));
    }

    private boolean titleMatchesQuestion(String normalizedQuestion, Object rawTitle) {
        if (rawTitle == null) {
            return false;
        }

        String normalizedTitle = normalizeForMatching(String.valueOf(rawTitle));
        return normalizedTitle.length() >= 2 && normalizedQuestion.contains(normalizedTitle);
    }

    private String normalizeForMatching(String value) {
        if (value == null) {
            return "";
        }

        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[\\s\\p{Punct}，。！？、；：‘’“”（）【】《》]+", "");
    }

    private Flux<ServerSentEvent<String>> handleChatError(String sessionId, String question, Throwable ex) {
        String message = ex instanceof ChatFlowException
                ? ex.getMessage()
                : "回答生成失败，请稍后重试。";

        log.error("AI chat failed, question={}", question, ex);
        saveFailedTurn(sessionId, question, message);

        return Flux.just(ServerSentEvent.<String>builder()
                .event("error")
                .data(message)
                .build());
    }

    private void recordSuccessfulTurn(String sessionId, String question, String answer, String sourcesJson,
            String systemPrompt) {
        try {
            chatHistoryService.saveTurn(sessionId, question, answer, sourcesJson);
        } catch (Exception ex) {
            log.warn("Failed to save chat history, sessionId={}", sessionId, ex);
        }

        try {
            dailyStatsService.addTokens(estimateTokens(systemPrompt + question + answer));
        } catch (Exception ex) {
            log.warn("Failed to record token stats, sessionId={}", sessionId, ex);
        }
    }

    private void saveFailedTurn(String sessionId, String question, String message) {
        try {
            chatHistoryService.saveTurn(sessionId, question, "[错误] " + message, "[]");
        } catch (Exception saveEx) {
            log.warn("Failed to save failed chat turn, sessionId={}", sessionId, saveEx);
        }
    }

    private int estimateTokens(String text) {
        int ascii = 0;
        int cjk = 0;
        for (char c : text.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) {
                cjk++;
            } else if (c < 128) {
                ascii++;
            }
        }
        int other = text.length() - ascii - cjk;
        return (int) (cjk / 1.5 + ascii / 4.0 + other / 3.0);
    }

    private static class ChatFlowException extends RuntimeException {
        ChatFlowException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
