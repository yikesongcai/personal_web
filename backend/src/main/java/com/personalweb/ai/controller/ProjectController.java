package com.personalweb.ai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.personalweb.ai.entity.Project;
import com.personalweb.ai.service.ProjectService;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<Project> listProjects() {
        return projectService.listAll();
    }

    @GetMapping("/{id}")
    public Project getProject(@PathVariable Long id) {
        return projectService.getById(id);
    }
}
