package com.personalweb.ai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.personalweb.ai.entity.Project;
import com.personalweb.ai.service.ProjectService;

@RestController
@RequestMapping("/api/admin/projects")
public class AdminProjectController {

    private final ProjectService projectService;

    public AdminProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<Project> list() {
        return projectService.listAll();
    }

    @PostMapping
    public Project create(@RequestBody Project project) {
        Long id = projectService.createProject(project);
        return projectService.getById(id);
    }

    @PutMapping("/{id}")
    public Project update(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        projectService.updateProject(project);
        return projectService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        projectService.deleteProject(id);
    }
}
