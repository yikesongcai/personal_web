package com.personalweb.ai.dto;

import java.util.List;

public class DashVectorDebugCleanupRequest {

    private Integer topK;
    private Integer maxQueries;
    private Boolean dryRun;
    private String idPrefix;
    private List<String> querySeeds;

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public Integer getMaxQueries() {
        return maxQueries;
    }

    public void setMaxQueries(Integer maxQueries) {
        this.maxQueries = maxQueries;
    }

    public Boolean getDryRun() {
        return dryRun;
    }

    public void setDryRun(Boolean dryRun) {
        this.dryRun = dryRun;
    }

    public String getIdPrefix() {
        return idPrefix;
    }

    public void setIdPrefix(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    public List<String> getQuerySeeds() {
        return querySeeds;
    }

    public void setQuerySeeds(List<String> querySeeds) {
        this.querySeeds = querySeeds;
    }
}