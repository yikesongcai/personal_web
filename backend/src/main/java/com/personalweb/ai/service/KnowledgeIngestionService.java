package com.personalweb.ai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.personalweb.ai.dto.KnowledgeItemRequest;

@Service
public class KnowledgeIngestionService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeIngestionService.class);

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    public KnowledgeIngestionService(VectorStore vectorStore, EmbeddingModel embeddingModel) {
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
    }

    public void ingestMockProject() {
        String content = """
                项目名称: Sketched-AirDefense
                项目类型: 联邦学习安全研究
                技术标签: AirComp, Byzantine-Robust FL, Gradient Sketching, Self-Supervised Prediction
                摘要: 该项目专注于空中计算 (AirComp) 联邦学习中的拜占庭攻击防御机制，偏向算法优化方向，
                引入了 Gradient Sketching 与自监督预测技术，在高噪声与信道失真场景下提升聚合鲁棒性与收敛稳定性。
                关键创新:
                1) 在上行模拟聚合过程中对梯度进行压缩感知友好的草图编码，降低带宽占用。
                2) 使用轻量级自监督模块预测客户端更新一致性，抑制恶意更新对全局模型的偏移。
                3) 在异步更新与非独立同分布数据下保持较高精度与攻击容忍率。
                实验结果: 在 CIFAR-10 与 Tiny-ImageNet 的多种攻击强度设置中，
                相比传统 Krum/Median 系列基线，Sketched-AirDefense 在精度与稳定性上取得显著提升。
                """;

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceType", "project");
        metadata.put("projectName", "Sketched-AirDefense");
        metadata.put("url", "/projects/sketched-airdefense");

        Document rawDocument = new Document(content, metadata);
        storeDocuments(List.of(rawDocument));
    }

    public int ingestBatch(List<KnowledgeItemRequest> items) {
        List<Document> rawDocuments = items.stream()
                .map(this::toDocument)
                .collect(Collectors.toList());

        return storeDocuments(rawDocuments);
    }

    private Document toDocument(KnowledgeItemRequest item) {
        String tags = item.getTags() == null ? "" : String.join(", ", item.getTags());
        String summary = item.getSummary() == null ? "" : item.getSummary();

        String content = """
                标题: %s
                类型: %s
                URL: %s
                摘要: %s
                标签: %s
                正文:
                %s
                """.formatted(item.getTitle(), item.getType(), item.getUrl(), summary, tags, item.getContent());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceType", item.getType());
        metadata.put("title", item.getTitle());
        metadata.put("url", item.getUrl());

        return new Document(content, metadata);
    }

    private int storeDocuments(List<Document> rawDocuments) {
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> splitDocuments = splitter.apply(rawDocuments);

        for (Document doc : splitDocuments) {
            float[] vector = embeddingModel.embed(doc.getText());
            log.info("Mock 文档切片向量维度: {}", vector.length);
        }

        vectorStore.add(splitDocuments);
        log.info("知识入库完成，切片数量: {}", splitDocuments.size());
        return splitDocuments.size();
    }
}
