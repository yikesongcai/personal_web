package com.personalweb.ai.dto;

public class SourceReference {

    private String title;
    private String url;
    private String sourceType;

    public SourceReference() {
    }

    public SourceReference(String title, String url, String sourceType) {
        this.title = title;
        this.url = url;
        this.sourceType = sourceType;
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

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
