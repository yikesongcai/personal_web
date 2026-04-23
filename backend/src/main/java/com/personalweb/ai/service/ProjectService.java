package com.personalweb.ai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.personalweb.ai.dao.ProjectDao;
import com.personalweb.ai.entity.Project;

@Service
public class ProjectService {

    private final ProjectDao projectDao;
    private final VectorStore vectorStore;
    private final SystemLogService systemLogService;

    public ProjectService(ProjectDao projectDao, VectorStore vectorStore, SystemLogService systemLogService) {
        this.projectDao = projectDao;
        this.vectorStore = vectorStore;
        this.systemLogService = systemLogService;
    }

    public List<Project> listAll() {
        return projectDao.findAll();
    }

    public Project getById(Long id) {
        return projectDao.findById(id);
    }

    @Transactional
    public Long createProject(Project project) {
        Long id = projectDao.insert(project);
        project.setId(id);
        syncToVectorStore(project);
        systemLogService.info("Project", "创建项目: [" + id + "] " + project.getTitle());
        return id;
    }

    @Transactional
    public void updateProject(Project project) {
        projectDao.update(project);
        syncToVectorStore(project);
        systemLogService.info("Project", "更新项目: [" + project.getId() + "] " + project.getTitle());
    }

    @Transactional
    public void deleteProject(Long id) {
        projectDao.delete(id);
        try {
            vectorStore.delete(List.of("project_" + id));
        } catch (Exception ignore) {
        }
        systemLogService.warn("Project", "删除项目: id=" + id);
    }

    private void syncToVectorStore(Project project) {
        String content = """
                项目名称: %s
                框架: %s
                在线地址: %s
                开源地址: %s
                正文:
                %s
                """.formatted(project.getTitle(), project.getFrameworks(), project.getOnlineUrl(), project.getGithubUrl(), project.getContent());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceType", "project");
        metadata.put("title", project.getTitle());
        metadata.put("url", "/projects/" + project.getId());

        Document doc = new Document("project_" + project.getId(), content, metadata);
        
        try {
            vectorStore.delete(List.of(doc.getId()));
        } catch (Exception ignore) {}
        
        vectorStore.add(List.of(doc));
    }
}
