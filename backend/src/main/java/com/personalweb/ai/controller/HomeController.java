package com.personalweb.ai.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.personalweb.ai.entity.Article;
import com.personalweb.ai.entity.Project;
import com.personalweb.ai.service.ArticleService;
import com.personalweb.ai.service.ProjectService;
import com.personalweb.ai.service.SiteConfigService;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final ProjectService projectService;
    private final ArticleService articleService;
    private final SiteConfigService siteConfigService;

    public HomeController(ProjectService projectService, ArticleService articleService, SiteConfigService siteConfigService) {
        this.projectService = projectService;
        this.articleService = articleService;
        this.siteConfigService = siteConfigService;
    }

    @GetMapping
    public Map<String, Object> homeData() {
        List<Project> featured = projectService.listFeatured();
        // Fallback: if no featured, use latest 3
        if (featured.isEmpty()) {
            featured = projectService.listAll().stream().limit(3).toList();
        }
        List<Article> featuredArticles = articleService.listFeatured();
        if (featuredArticles.isEmpty()) {
            featuredArticles = articleService.listAll().stream().limit(3).toList();
        }
        return Map.of(
            "config", siteConfigService.getAll(),
            "projects", featured,
            "articles", featuredArticles
        );
    }
}
