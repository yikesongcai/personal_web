package com.personalweb.ai.vectorstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.util.StringUtils;

import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.models.Doc;
import com.aliyun.dashvector.models.DocOpResult;
import com.aliyun.dashvector.models.Vector;
import com.aliyun.dashvector.models.requests.DeleteDocRequest;
import com.aliyun.dashvector.models.requests.QueryDocRequest;
import com.aliyun.dashvector.models.requests.UpsertDocRequest;
import com.aliyun.dashvector.models.responses.Response;
import com.personalweb.ai.exception.BusinessException;

public class DashVectorVectorStore implements VectorStore {

    private static final String CONTENT_FIELD = "content";

    private final DashVectorCollection collection;
    private final EmbeddingModel embeddingModel;

    public DashVectorVectorStore(DashVectorCollection collection, EmbeddingModel embeddingModel) {
        this.collection = collection;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public String getName() {
        return "dashvector";
    }

    @Override
    public void add(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        for (Document document : documents) {
            float[] embedding = embeddingModel.embed(document.getText());
            Vector vector = Vector.builder().value(toNumberList(embedding)).build();

            Map<String, Object> fields = new HashMap<>(document.getMetadata());
            fields.put(CONTENT_FIELD, document.getText());

            Doc.DocBuilder builder = Doc.builder()
                    .id(document.getId())
                    .fields(fields);

            builder.vector(vector);

            Doc dashDoc = builder.build();

            UpsertDocRequest request = UpsertDocRequest.builder()
                    .doc(dashDoc)
                    .build();

            Response<List<DocOpResult>> response;
            try {
                response = collection.upsert(request);
            } catch (Exception ex) {
                throw BusinessException.externalService("DASHVECTOR_UPSERT_ERROR",
                        "DashVector 入库异常: " + ex.getMessage());
            }

            if (!Boolean.TRUE.equals(response.isSuccess())) {
                throw BusinessException.externalService("DASHVECTOR_UPSERT_FAILED",
                        "DashVector 入库失败: " + response.getMessage());
            }
        }
    }

    @Override
    public void delete(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        DeleteDocRequest request = DeleteDocRequest.builder()
                .ids(ids)
                .build();

        try {
            Response<List<DocOpResult>> response = collection.delete(request);
            if (!Boolean.TRUE.equals(response.isSuccess())) {
                throw BusinessException.externalService("DASHVECTOR_DELETE_FAILED",
                        "DashVector 删除失败: " + response.getMessage());
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.externalService("DASHVECTOR_DELETE_ERROR",
                    "DashVector 删除异常: " + ex.getMessage());
        }
    }

    @Override
    public void delete(Filter.Expression filterExpression) {
        throw BusinessException.badRequest("DASHVECTOR_FILTER_DELETE_UNSUPPORTED",
                "DashVector 适配器暂不支持按 Filter.Expression 删除。");
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        if (request == null || !StringUtils.hasText(request.getQuery())) {
            return List.of();
        }

        float[] queryEmbedding = embeddingModel.embed(request.getQuery());
        Vector queryVector = Vector.builder().value(toNumberList(queryEmbedding)).build();

        QueryDocRequest.QueryDocRequestBuilder queryBuilder = QueryDocRequest.builder()
                .topk(Math.max(1, request.getTopK()))
                .includeVector(false);

        queryBuilder.vector(queryVector);

        QueryDocRequest query = queryBuilder.build();

        Response<List<Doc>> response;
        try {
            response = collection.query(query);
        } catch (Exception ex) {
            throw BusinessException.externalService("DASHVECTOR_QUERY_ERROR",
                    "DashVector 检索异常: " + ex.getMessage());
        }

        if (!Boolean.TRUE.equals(response.isSuccess())) {
            throw BusinessException.externalService("DASHVECTOR_QUERY_FAILED",
                    "DashVector 检索失败: " + response.getMessage());
        }
        if (response.getOutput() == null) {
            return List.of();
        }

        List<Document> results = new ArrayList<>();
        for (Doc doc : response.getOutput()) {
            double score = doc.getScore();
            if (request.getSimilarityThreshold() != SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL
                    && score < request.getSimilarityThreshold()) {
                continue;
            }

            Map<String, Object> metadata = doc.getFields() == null ? new HashMap<>() : new HashMap<>(doc.getFields());
            metadata.putIfAbsent("dashvectorDocId", doc.getId());

            String content = Objects.toString(metadata.get(CONTENT_FIELD), "");
            if (!StringUtils.hasText(content)) {
                content = Objects.toString(metadata.getOrDefault("title", "未命名内容"));
            }

            results.add(Document.builder()
                    .id(doc.getId())
                    .text(content)
                    .metadata(metadata)
                    .score(score)
                    .build());
        }

        return results;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> java.util.Optional<T> getNativeClient() {
        return java.util.Optional.of((T) collection);
    }

    private List<Float> toNumberList(float[] vector) {
        List<Float> values = new ArrayList<>(vector.length);
        for (float value : vector) {
            values.add(value);
        }
        return values;
    }
}
