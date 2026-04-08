package com.personalweb.ai.exception;

import java.time.LocalDateTime;

public class ApiErrorResponse {

    private final String code;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;

    public ApiErrorResponse(String code, String message, String path, LocalDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
