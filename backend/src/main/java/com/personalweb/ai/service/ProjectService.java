package com.personalweb.ai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.personalweb.ai.dao.KnowledgeChunkDao;
import com.personalweb.ai.dao.ProjectDao;
import com.personalweb.ai.entity.KnowledgeChunk;
import com.personalweb.ai.entity.Project;

@Service
public class ProjectService {

    private final ProjectDao projectDao;
    private final VectorStore vectorStore;
    private final KnowledgeChunkDao knowledgeChunkDao;
    private final SystemLogService systemLogService;

    public ProjectService(ProjectDao projectDao, VectorStore vectorStore,
                          KnowledgeChunkDao knowledgeChunkDao, SystemLogService systemLogService) {
        this.projectDao = projectDao;
        this.vectorStore = vectorStore;
        this.knowledgeChunkDao = knowledgeChunkDao;
        this.systemLogService = systemLogService;
    }

    public List<Project> listAll() {
        return projectDao.findAll();
    }

    public List<Project> listFeatured() {
        return projectDao.findFeatured();
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
        String docId = "project_" + id;
        try {
            vectorStore.delete(List.of(docId));
        } catch (Exception ignore) {
        }
        try {
            knowledgeChunkDao.deleteByDocId(docId);
        } catch (Exception ignore) {
        }
        systemLogService.warn("Project", "删除项目: id=" + id);
    }

    public void syncToVectorStore(Project project) {
        String docId = "project_" + project.getId();
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

        Document doc = new Document(docId, content, metadata);

        try {
            vectorStore.delete(List.of(docId));
        } catch (Exception ignore) {}

        vectorStore.add(List.of(doc));

        // Mirror to MySQL
        knowledgeChunkDao.deleteByDocId(docId);
        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setDocId(docId);
        chunk.setDocType("project");
        chunk.setTitle(project.getTitle());
        chunk.setContent(content);
        chunk.setChunkIndex(0);
        knowledgeChunkDao.insert(chunk);
    }
}
