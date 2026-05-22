package com.personalweb.ai.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.personalweb.ai.dao.KnowledgeChunkDao;
import com.personalweb.ai.dto.KnowledgeItemRequest;
import com.personalweb.ai.entity.Article;
import com.personalweb.ai.entity.KnowledgeChunk;
import com.personalweb.ai.entity.Project;
import com.personalweb.ai.service.ArticleService;
import com.personalweb.ai.service.KnowledgeIngestionService;
import com.personalweb.ai.service.ProjectService;
import com.personalweb.ai.service.SystemLogService;

@RestController
@RequestMapping("/api/admin/knowledge")
public class AdminKnowledgeController {

    private final KnowledgeChunkDao knowledgeChunkDao;
    private final KnowledgeIngestionService knowledgeIngestionService;
    private final ArticleService articleService;
    private final ProjectService projectService;
    private final SystemLogService systemLogService;

    public AdminKnowledgeController(KnowledgeChunkDao knowledgeChunkDao,
                                     KnowledgeIngestionService knowledgeIngestionService,
                                     ArticleService articleService,
                                     ProjectService projectService,
                                     SystemLogService systemLogService) {
        this.knowledgeChunkDao = knowledgeChunkDao;
        this.knowledgeIngestionService = knowledgeIngestionService;
        this.articleService = articleService;
        this.projectService = projectService;
        this.systemLogService = systemLogService;
    }

    @GetMapping("/chunks")
    public Map<String, Object> listChunks(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        List<KnowledgeChunk> chunks = knowledgeChunkDao.findAll(page, size);
        long total = knowledgeChunkDao.count();
        return Map.of("data", chunks, "total", total, "page", page, "size", size);
    }

    @DeleteMapping("/chunks/{id}")
    public Map<String, String> deleteChunk(@PathVariable Long id) {
        KnowledgeChunk chunk = knowledgeChunkDao.findById(id);
        if (chunk != null) {
            knowledgeChunkDao.deleteById(id);
            systemLogService.warn("Knowledge", "删除知识块: docId=" + chunk.getDocId() + " chunk=" + chunk.getChunkIndex());
        }
        return Map.of("ok", "deleted");
    }

    @DeleteMapping("/docs/{docId}")
    public Map<String, String> deleteDoc(@PathVariable String docId) {
        long count = knowledgeChunkDao.countByDocId(docId);
        knowledgeChunkDao.deleteByDocId(docId);
        systemLogService.warn("Knowledge", "删除知识文档: " + docId + " (" + count + " 个切块)");
        return Map.of("ok", "deleted", "chunks", String.valueOf(count));
    }

    @PostMapping("/reindex/article/{id}")
    public Map<String, String> reindexArticle(@PathVariable Long id) {
        Article article = articleService.getById(id);
        if (article == null) {
            return Map.of("error", "文章不存在");
        }
        articleService.syncToVectorStore(article);
        systemLogService.info("Knowledge", "重新索引文章: [" + id + "] " + article.getTitle());
        return Map.of("ok", "reindexed", "title", article.getTitle());
    }

    @PostMapping("/reindex/project/{id}")
    public Map<String, String> reindexProject(@PathVariable Long id) {
        Project project = projectService.getById(id);
        if (project == null) {
            return Map.of("error", "项目不存在");
        }
        projectService.syncToVectorStore(project);
        systemLogService.info("Knowledge", "重新索引项目: [" + id + "] " + project.getTitle());
        return Map.of("ok", "reindexed", "title", project.getTitle());
    }

    @PostMapping("/ingest")
    public Map<String, Object> manualIngest(@RequestBody KnowledgeItemRequest item) {
        int count = knowledgeIngestionService.ingestBatch(List.of(item));
        systemLogService.info("Knowledge", "手动入库知识: " + item.getTitle() + " (" + count + " 切块)");
        return Map.of("ok", "ingested", "chunks", count);
    }

    @PostMapping("/reindex-all")
    public Map<String, Object> reindexAll() {
        int total = 0;
        List<Article> articles = articleService.listAll();
        for (Article a : articles) {
            articleService.syncToVectorStore(a);
            total++;
        }
        List<Project> projects = projectService.listAll();
        for (Project p : projects) {
            projectService.syncToVectorStore(p);
            total++;
        }
        systemLogService.info("Knowledge", "批量重新索引完成: " + total + " 篇");
        return Map.of("ok", "reindexed_all", "total", total);
    }
}
