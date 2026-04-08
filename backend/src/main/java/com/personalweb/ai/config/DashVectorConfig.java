package com.personalweb.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.personalweb.ai.vectorstore.DashVectorVectorStore;

@Configuration
public class DashVectorConfig {

    private static final Logger log = LoggerFactory.getLogger(DashVectorConfig.class);

    @Value("${spring.ai.vectorstore.dashvector.api-key}")
    private String apiKey;

    @Value("${spring.ai.vectorstore.dashvector.endpoint}")
    private String endpoint;

    @Value("${spring.ai.vectorstore.dashvector.collection-name}")
    private String collectionName;

    @Bean(destroyMethod = "close")
    public DashVectorClient dashVectorClient() throws Exception {
        return new DashVectorClient(apiKey, endpoint);
    }

    @Bean
    public DashVectorCollection dashVectorCollection(DashVectorClient dashVectorClient) {
        DashVectorCollection collection = dashVectorClient.get(collectionName);
        if (collection == null || !Boolean.TRUE.equals(collection.isSuccess())) {
            throw new IllegalStateException("无法连接 DashVector Collection: " + collectionName);
        }

        var meta = collection.getCollectionMeta();
        log.info("DashVector Collection 已连接: name={}, dimension={}, dataType={}, metric={}",
                meta.getName(),
                meta.getDimension(),
                meta.getDataType(),
            meta.getMetric());
        return collection;
    }

    @Bean
    public VectorStore dashVectorVectorStore(DashVectorCollection collection, EmbeddingModel embeddingModel) {
        return new DashVectorVectorStore(collection, embeddingModel);
    }
}
