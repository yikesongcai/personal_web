package com.personalweb.ai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.personalweb.ai.entity.Article;
import com.personalweb.ai.service.ArticleService;

@RestController
@RequestMapping("/api/admin/articles")
public class AdminArticleController {

    private final ArticleService articleService;

    public AdminArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public List<Article> list() {
        return articleService.listAll();
    }

    @PostMapping
    public Article create(@RequestBody Article article) {
        Long id = articleService.createArticle(article);
        return articleService.getById(id);
    }

    @PutMapping("/{id}")
    public Article update(@PathVariable Long id, @RequestBody Article article) {
        article.setId(id);
        articleService.updateArticle(article);
        return articleService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        articleService.deleteArticle(id);
    }
}
