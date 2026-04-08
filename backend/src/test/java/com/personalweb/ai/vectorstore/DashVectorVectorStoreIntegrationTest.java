package com.personalweb.ai.vectorstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;

import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.models.Doc;
import com.aliyun.dashvector.models.DocOpResult;
import com.aliyun.dashvector.models.Vector;
import com.aliyun.dashvector.models.responses.Response;
import com.personalweb.ai.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class DashVectorVectorStoreIntegrationTest {

    @Mock
    private DashVectorCollection collection;

    @Mock
    private EmbeddingModel embeddingModel;

    private DashVectorVectorStore vectorStore;

    @BeforeEach
    void setUp() {
        vectorStore = new DashVectorVectorStore(collection, embeddingModel);
    }

    @Test
    void addShouldUpsertIntoDashVector() {
        when(embeddingModel.embed(any(String.class))).thenReturn(new float[] { 0.11f, 0.22f, 0.33f });
        when(collection.upsert(any())).thenReturn(Response.create(0, "ok", "req-1", List.of(new DocOpResult())));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Sketched-AirDefense");
        metadata.put("url", "/projects/sketched-airdefense");

        Document document = Document.builder()
                .id("doc-1")
                .text("Sketched-AirDefense 使用 Gradient Sketching 进行鲁棒聚合。")
                .metadata(metadata)
                .build();

        vectorStore.add(List.of(document));

        verify(collection).upsert(any());
    }

    @Test
    void similaritySearchShouldReturnThresholdFilteredResult() {
        when(embeddingModel.embed(any(String.class))).thenReturn(new float[] { 0.5f, 0.7f, 0.9f });

        Doc highScoreDoc = Doc.builder()
                .id("result-1")
                .score(0.92f)
                .vector(Vector.builder().value(List.of(0.1f, 0.2f, 0.3f)).build())
                .fields(Map.of(
                        "content", "该项目聚焦 AirComp 联邦学习防御。",
                        "title", "Sketched-AirDefense",
                        "url", "/projects/sketched-airdefense",
                        "sourceType", "project"))
                .build();

        Doc lowScoreDoc = Doc.builder()
                .id("result-2")
                .score(0.21f)
                .vector(Vector.builder().value(List.of(0.4f, 0.5f, 0.6f)).build())
                .fields(Map.of(
                        "content", "无关内容",
                        "title", "Noise",
                        "url", "/projects/noise",
                        "sourceType", "project"))
                .build();

        when(collection.query(any())).thenReturn(Response.create(0, "ok", "req-2", List.of(highScoreDoc, lowScoreDoc)));

        SearchRequest request = SearchRequest.builder()
                .query("联邦学习防御")
                .topK(2)
                .similarityThreshold(0.8)
                .build();

        List<Document> documents = vectorStore.similaritySearch(request);

        assertEquals(1, documents.size());
        assertEquals("result-1", documents.get(0).getId());
    }

    @Test
    void deleteShouldThrowBusinessExceptionWhenDashVectorFails() {
        when(collection.delete(any())).thenReturn(Response.create(500, "delete failed", "req-3", List.of()));

        assertThrows(BusinessException.class, () -> vectorStore.delete(List.of("doc-1")));
    }
}
