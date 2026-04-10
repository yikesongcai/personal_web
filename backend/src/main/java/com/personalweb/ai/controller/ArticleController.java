package com.personalweb.ai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.personalweb.ai.entity.Article;
import com.personalweb.ai.service.ArticleService;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public List<Article> listArticles() {
        return articleService.listAll();
    }

    @GetMapping("/{id}")
    public Article getArticle(@PathVariable Long id) {
        return articleService.getById(id);
    }
}
