package com.personalweb.ai.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

import com.personalweb.ai.entity.Article;
import com.personalweb.ai.entity.Project;
import com.personalweb.ai.service.ArticleService;
import com.personalweb.ai.service.DailyStatsService;
import com.personalweb.ai.service.ProjectService;
import com.personalweb.ai.service.SiteConfigService;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final ProjectService projectService;
    private final ArticleService articleService;
    private final SiteConfigService siteConfigService;
    private final DailyStatsService dailyStatsService;

    public HomeController(ProjectService projectService, ArticleService articleService,
                          SiteConfigService siteConfigService, DailyStatsService dailyStatsService) {
        this.projectService = projectService;
        this.articleService = articleService;
        this.siteConfigService = siteConfigService;
        this.dailyStatsService = dailyStatsService;
    }

    @GetMapping
    public Map<String, Object> homeData(ServerHttpRequest request) {
        String ip = getClientIp(request);
        dailyStatsService.addVisit(ip);
        List<Project> featured = projectService.listFeatured();
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

    private String getClientIp(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }
}
