package com.personalweb.ai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.personalweb.ai.entity.Project;
import com.personalweb.ai.service.DailyStatsService;
import com.personalweb.ai.service.ProjectService;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final DailyStatsService dailyStatsService;

    public ProjectController(ProjectService projectService, DailyStatsService dailyStatsService) {
        this.projectService = projectService;
        this.dailyStatsService = dailyStatsService;
    }

    @GetMapping
    public List<Project> listProjects() {
        dailyStatsService.addVisit();
        return projectService.listAll();
    }

    @GetMapping("/{id}")
    public Project getProject(@PathVariable Long id) {
        return projectService.getById(id);
    }
}
