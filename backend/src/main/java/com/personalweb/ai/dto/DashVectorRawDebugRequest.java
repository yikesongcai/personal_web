package com.personalweb.ai.dto;

public class DashVectorRawDebugRequest {

    private String content;
    private String question;
    private String mode = "vector-float";
    private String writeOp = "upsert";
    private String vectorKey;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getWriteOp() {
        return writeOp;
    }

    public void setWriteOp(String writeOp) {
        this.writeOp = writeOp;
    }

    public String getVectorKey() {
        return vectorKey;
    }

    public void setVectorKey(String vectorKey) {
        this.vectorKey = vectorKey;
    }
}
