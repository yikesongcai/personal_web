package com.personalweb.ai.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class BatchIngestionRequest {

    @Valid
    @NotEmpty(message = "items 不能为空")
    private List<KnowledgeItemRequest> items;

    public List<KnowledgeItemRequest> getItems() {
        return items;
    }

    public void setItems(List<KnowledgeItemRequest> items) {
        this.items = items;
    }
}
