package com.personalweb.ai.service;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.models.Doc;
import com.aliyun.dashvector.models.DocOpResult;
import com.aliyun.dashvector.models.Vector;
import com.aliyun.dashvector.models.requests.DeleteDocRequest;
import com.aliyun.dashvector.models.requests.InsertDocRequest;
import com.aliyun.dashvector.models.requests.QueryDocRequest;
import com.aliyun.dashvector.models.requests.UpsertDocRequest;
import com.aliyun.dashvector.models.responses.Response;
import com.personalweb.ai.dto.DashVectorDebugCleanupRequest;
import com.personalweb.ai.dto.DashVectorRawDebugRequest;
import com.personalweb.ai.exception.BusinessException;

@Service
public class DashVectorRawDebugService {

    private static final int DEFAULT_TOP_K = 50;
    private static final int DEFAULT_MAX_QUERIES = 4;
    private static final int MAX_TOP_K = 200;
    private static final int MAX_QUERY_ROUNDS = 10;
    private static final int DELETE_BATCH_SIZE = 50;
    private static final String DEFAULT_ID_PREFIX = "debug-";
    private static final List<String> DEFAULT_CLEANUP_SEEDS = List.of(
            "debug raw",
            "sourceType debug",
            "/debug/raw",
            "debug-doc");

    private static final Logger log = LoggerFactory.getLogger(DashVectorRawDebugService.class);

    private final DashVectorCollection collection;
    private final EmbeddingModel embeddingModel;

    public DashVectorRawDebugService(DashVectorCollection collection, EmbeddingModel embeddingModel) {
        this.collection = collection;
        this.embeddingModel = embeddingModel;
    }

    public Map<String, Object> rawDebugOnce(DashVectorRawDebugRequest req) {
        String content = StringUtils.hasText(req.getContent())
                ? req.getContent()
                : "Sketched-AirDefense 专注于空中计算联邦学习中的拜占庭鲁棒聚合。";
        String question = StringUtils.hasText(req.getQuestion())
                ? req.getQuestion()
                : "Sketched-AirDefense 的核心创新是什么？";
        String mode = StringUtils.hasText(req.getMode()) ? req.getMode().trim().toLowerCase() : "vector-float";
        String writeOp = StringUtils.hasText(req.getWriteOp()) ? req.getWriteOp().trim().toLowerCase() : "upsert";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().toString());
        result.put("mode", mode);
        result.put("writeOp", writeOp);

        var meta = collection.getCollectionMeta();
        result.put("collection", Map.of(
                "name", meta.getName(),
                "dimension", meta.getDimension(),
                "dataType", String.valueOf(meta.getDataType()),
            "metric", String.valueOf(meta.getMetric())));

        float[] embedding = embeddingModel.embed(content);
        Vector vector = Vector.builder().value(buildNumberList(embedding, mode)).build();

        Map<String, Object> fields = new HashMap<>();
        fields.put("title", "debug-doc");
        fields.put("sourceType", "debug");
        fields.put("url", "/debug/raw");
        fields.put("content", content);

        String docId = "debug-" + UUID.randomUUID();
        Doc.DocBuilder docBuilder = Doc.builder()
                .id(docId)
                .fields(fields)
                .vector(vector);

        Doc doc = docBuilder.build();

        Map<String, Object> beforeSubmit = new LinkedHashMap<>();
        beforeSubmit.put("docId", doc.getId());
        beforeSubmit.put("embeddingLength", embedding.length);
        beforeSubmit.put("vectorValueType", buildNumberList(embedding, mode).isEmpty()
                ? "unknown"
                : buildNumberList(embedding, mode).get(0).getClass().getSimpleName());
        beforeSubmit.put("hasDocVector", doc.getVector() != null);
        beforeSubmit.put("fieldKeys", doc.getFields().keySet());
        result.put("beforeSubmit", beforeSubmit);

        log.info("DashVector RAW Debug before submit: {}", beforeSubmit);

        Object writeRequest;
        Response<List<DocOpResult>> writeResponse;
        try {
            if ("insert".equals(writeOp)) {
                InsertDocRequest reqObj = InsertDocRequest.builder().doc(doc).build();
                writeRequest = reqObj;
                writeResponse = collection.insert(reqObj);
            } else {
                UpsertDocRequest reqObj = UpsertDocRequest.builder().doc(doc).build();
                writeRequest = reqObj;
                writeResponse = collection.upsert(reqObj);
            }

            result.put("writeRequestProtoSummary", summarizeWriteProto(writeRequest, meta));
            result.put("writeResponse", summarizeWriteResponse(writeResponse));
        } catch (Exception ex) {
            result.put("writeException", ex.getClass().getSimpleName() + ": " + ex.getMessage());
            log.warn("DashVector RAW write failed: {}", ex.getMessage());
            return result;
        }

        try {
            Vector queryVector = Vector.builder().value(buildNumberList(embeddingModel.embed(question), mode)).build();

            QueryDocRequest.QueryDocRequestBuilder queryBuilder = QueryDocRequest.builder()
                    .topk(1)
                    .includeVector(false)
                    .vector(queryVector);

            QueryDocRequest queryRequest = queryBuilder.build();
            Response<List<Doc>> queryResponse = collection.query(queryRequest);

            result.put("queryResponse", summarizeQueryResponse(queryResponse));
            if (Boolean.TRUE.equals(queryResponse.isSuccess()) && queryResponse.getOutput() != null && !queryResponse.getOutput().isEmpty()) {
                Doc hit = queryResponse.getOutput().get(0);
                result.put("firstHit", Map.of(
                        "id", hit.getId(),
                        "score", hit.getScore(),
                        "fieldKeys", hit.getFields() == null ? List.of() : hit.getFields().keySet()));
            }
        } catch (Exception ex) {
            result.put("queryException", ex.getClass().getSimpleName() + ": " + ex.getMessage());
            log.warn("DashVector RAW query failed: {}", ex.getMessage());
        }

        return result;
    }

    public Map<String, Object> cleanupDebugDocs(DashVectorDebugCleanupRequest req) {
        int topK = clamp(resolveInt(req == null ? null : req.getTopK(), DEFAULT_TOP_K), 1, MAX_TOP_K);
        int maxQueries = clamp(resolveInt(req == null ? null : req.getMaxQueries(), DEFAULT_MAX_QUERIES), 1, MAX_QUERY_ROUNDS);
        boolean dryRun = req != null && Boolean.TRUE.equals(req.getDryRun());
        String idPrefix = StringUtils.hasText(req == null ? null : req.getIdPrefix())
                ? req.getIdPrefix().trim()
                : DEFAULT_ID_PREFIX;

        List<String> seeds = buildCleanupSeeds(req == null ? null : req.getQuerySeeds());
        if (seeds.size() > maxQueries) {
            seeds = seeds.subList(0, maxQueries);
        }

        Set<String> candidates = new LinkedHashSet<>();
        int scannedHits = 0;
        List<Map<String, Object>> querySummaries = new ArrayList<>();

        for (String seed : seeds) {
            List<Doc> hits = queryBySeed(seed, topK);
            scannedHits += hits.size();

            int matched = 0;
            for (Doc hit : hits) {
                if (isDebugDoc(hit, idPrefix)) {
                    matched++;
                    candidates.add(hit.getId());
                }
            }

            querySummaries.add(Map.of(
                    "seed", seed,
                    "hitCount", hits.size(),
                    "matchedDebugCount", matched));
        }

        int deletedCount = 0;
        List<String> deletedIds = new ArrayList<>();
        if (!dryRun && !candidates.isEmpty()) {
            List<String> allIds = new ArrayList<>(candidates);
            for (int i = 0; i < allIds.size(); i += DELETE_BATCH_SIZE) {
                List<String> batch = allIds.subList(i, Math.min(i + DELETE_BATCH_SIZE, allIds.size()));
                deleteByIds(batch);
                deletedIds.addAll(batch);
            }
            deletedCount = deletedIds.size();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().toString());
        result.put("dryRun", dryRun);
        result.put("topK", topK);
        result.put("maxQueries", maxQueries);
        result.put("idPrefix", idPrefix);
        result.put("querySummaries", querySummaries);
        result.put("scannedHits", scannedHits);
        result.put("candidateCount", candidates.size());
        result.put("deletedCount", deletedCount);
        result.put("candidateIds", new ArrayList<>(candidates));
        if (!dryRun) {
            result.put("deletedIds", deletedIds);
        }
        return result;
    }

    private Map<String, Object> summarizeWriteResponse(Response<List<DocOpResult>> response) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("success", response.isSuccess());
        summary.put("code", response.getCode());
        summary.put("message", response.getMessage());
        summary.put("requestId", response.getRequestId());
        summary.put("outputSize", response.getOutput() == null ? 0 : response.getOutput().size());
        return summary;
    }

    private Map<String, Object> summarizeQueryResponse(Response<List<Doc>> response) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("success", response.isSuccess());
        summary.put("code", response.getCode());
        summary.put("message", response.getMessage());
        summary.put("requestId", response.getRequestId());
        summary.put("hitCount", response.getOutput() == null ? 0 : response.getOutput().size());
        return summary;
    }

    private Map<String, Object> summarizeWriteProto(Object requestObj, Object collectionMeta) {
        Map<String, Object> summary = new LinkedHashMap<>();
        try {
            Object proto = invokeToProto(requestObj, collectionMeta);

            summary.put("protoClass", proto.getClass().getName());
            summary.put("protoText", String.valueOf(proto));
            enrichProtoVectorInfo(proto, summary);
        } catch (Exception ex) {
            Throwable root = ex.getCause() == null ? ex : ex.getCause();
            summary.put("protoError", ex.getClass().getSimpleName() + ": " + ex.getMessage());
            summary.put("protoRootCause", root.getClass().getSimpleName() + ": " + root.getMessage());
        }
        return summary;
    }

    private Object invokeToProto(Object requestObj, Object collectionMeta) throws Exception {
        for (Method method : requestObj.getClass().getMethods()) {
            if (!"toProto".equals(method.getName())) {
                continue;
            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 2
                    && parameterTypes[0].isAssignableFrom(collectionMeta.getClass())
                    && parameterTypes[1].getName().contains("DocOpResult$DocOp")) {
                return method.invoke(requestObj, collectionMeta, com.aliyun.dashvector.proto.DocOpResult.DocOp.upsert);
            }

            if (parameterTypes.length == 1 && parameterTypes[0].getName().contains("CollectionInfo$DataType")) {
                Method getDataType = collectionMeta.getClass().getMethod("getDataType");
                Object dataType = getDataType.invoke(collectionMeta);
                return method.invoke(requestObj, dataType);
            }
        }

        throw new NoSuchMethodException("未找到可用的 toProto 方法签名");
    }

    private void enrichProtoVectorInfo(Object proto, Map<String, Object> summary) {
        try {
            Method getDocsCount = proto.getClass().getMethod("getDocsCount");
            int docsCount = (int) getDocsCount.invoke(proto);
            summary.put("protoDocsCount", docsCount);
            if (docsCount <= 0) {
                return;
            }

            Method getDocs = proto.getClass().getMethod("getDocs", int.class);
            Object firstDoc = getDocs.invoke(proto, 0);

            Method hasVector = firstDoc.getClass().getMethod("hasVector");
            boolean firstDocHasVector = (boolean) hasVector.invoke(firstDoc);
            summary.put("protoFirstDocHasVector", firstDocHasVector);

            if (firstDocHasVector) {
                Method getVector = firstDoc.getClass().getMethod("getVector");
                Object vector = getVector.invoke(firstDoc);
                Method getValueCount = vector.getClass().getMethod("getValueCount");
                summary.put("protoFirstDocVectorValueCount", getValueCount.invoke(vector));
            }
        } catch (Exception ignore) {
            // 不阻塞主流程，尽力输出。
        }
    }

    private List<? extends Number> buildNumberList(float[] vector, String mode) {
        if (mode.endsWith("double")) {
            List<Double> doubles = new ArrayList<>(vector.length);
            for (float v : vector) {
                doubles.add((double) v);
            }
            return doubles;
        }

        List<Float> floats = new ArrayList<>(vector.length);
        for (float v : vector) {
            floats.add(v);
        }
        return floats;
    }

    private List<String> buildCleanupSeeds(List<String> querySeeds) {
        List<String> seeds = new ArrayList<>(DEFAULT_CLEANUP_SEEDS);
        if (querySeeds != null) {
            for (String seed : querySeeds) {
                if (StringUtils.hasText(seed)) {
                    seeds.add(seed.trim());
                }
            }
        }
        return seeds;
    }

    private List<Doc> queryBySeed(String seed, int topK) {
        try {
            float[] seedEmbedding = embeddingModel.embed(seed);
            Vector queryVector = Vector.builder().value(buildNumberList(seedEmbedding, "vector-float")).build();
            QueryDocRequest query = QueryDocRequest.builder()
                    .topk(topK)
                    .includeVector(false)
                    .vector(queryVector)
                    .build();

            Response<List<Doc>> response = collection.query(query);
            if (!Boolean.TRUE.equals(response.isSuccess())) {
                throw BusinessException.externalService("DASHVECTOR_QUERY_FAILED",
                        "DashVector 清理检索失败: " + response.getMessage());
            }
            if (response.getOutput() == null) {
                return List.of();
            }
            return response.getOutput();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.externalService("DASHVECTOR_QUERY_ERROR",
                    "DashVector 清理检索异常: " + ex.getMessage());
        }
    }

    private boolean isDebugDoc(Doc doc, String idPrefix) {
        if (doc == null || !StringUtils.hasText(doc.getId())) {
            return false;
        }

        if (StringUtils.hasText(idPrefix) && doc.getId().startsWith(idPrefix)) {
            return true;
        }

        Map<String, Object> fields = doc.getFields();
        if (fields == null || fields.isEmpty()) {
            return false;
        }

        String sourceType = safeString(fields.get("sourceType")).toLowerCase();
        String title = safeString(fields.get("title")).toLowerCase();
        String url = safeString(fields.get("url"));

        return "debug".equals(sourceType)
                || "debug-doc".equals(title)
                || "/debug/raw".equals(url);
    }

    private void deleteByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        DeleteDocRequest request = DeleteDocRequest.builder().ids(ids).build();
        try {
            Response<List<DocOpResult>> response = collection.delete(request);
            if (!Boolean.TRUE.equals(response.isSuccess())) {
                throw BusinessException.externalService("DASHVECTOR_DELETE_FAILED",
                        "DashVector 清理删除失败: " + response.getMessage());
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.externalService("DASHVECTOR_DELETE_ERROR",
                    "DashVector 清理删除异常: " + ex.getMessage());
        }
    }

    private int resolveInt(Integer raw, int defaultValue) {
        return raw == null ? defaultValue : raw;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String safeString(Object value) {
        return Objects.toString(value, "").trim();
    }
}
