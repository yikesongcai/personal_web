package com.personalweb.ai.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public class KnowledgeItemRequest {

    @NotBlank(message = "type 不能为空")
    private String type;

    @NotBlank(message = "title 不能为空")
    private String title;

    @NotBlank(message = "url 不能为空")
    private String url;

    private String summary;

    @NotBlank(message = "content 不能为空")
    private String content;

    private List<String> tags;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
