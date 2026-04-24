package com.personalweb.ai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.personalweb.ai.dao.ArticleDao;
import com.personalweb.ai.entity.Article;

@Service
public class ArticleService {

    private final ArticleDao articleDao;
    private final VectorStore vectorStore;
    private final SystemLogService systemLogService;

    public ArticleService(ArticleDao articleDao, VectorStore vectorStore, SystemLogService systemLogService) {
        this.articleDao = articleDao;
        this.vectorStore = vectorStore;
        this.systemLogService = systemLogService;
    }

    public List<Article> listAll() {
        return articleDao.findAll();
    }

    public List<Article> listFeatured() {
        return articleDao.findFeatured();
    }

    public Article getById(Long id) {
        return articleDao.findById(id);
    }

    @Transactional
    public Long createArticle(Article article) {
        Long id = articleDao.insert(article);
        article.setId(id);
        syncToVectorStore(article);
        systemLogService.info("Article", "创建文章: [" + id + "] " + article.getTitle());
        return id;
    }

    @Transactional
    public void updateArticle(Article article) {
        articleDao.update(article);
        syncToVectorStore(article);
        systemLogService.info("Article", "更新文章: [" + article.getId() + "] " + article.getTitle());
    }

    @Transactional
    public void deleteArticle(Long id) {
        articleDao.delete(id);
        try {
            vectorStore.delete(List.of("article_" + id));
        } catch (Exception ignore) {
        }
        systemLogService.warn("Article", "删除文章: id=" + id);
    }

    private void syncToVectorStore(Article article) {
        String content = """
                文章标题: %s
                标签: %s
                摘要: %s
                正文:
                %s
                """.formatted(article.getTitle(), article.getTags(), article.getSummary(), article.getContent());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceType", "article");
        metadata.put("title", article.getTitle());
        metadata.put("url", "/articles/" + article.getId());

        Document doc = new Document("article_" + article.getId(), content, metadata);
        
        try {
            vectorStore.delete(List.of(doc.getId()));
        } catch (Exception ignore) {}
        
        vectorStore.add(List.of(doc));
    }
}
