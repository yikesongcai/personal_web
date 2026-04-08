package com.personalweb.ai.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public BusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static BusinessException badRequest(String code, String message) {
        return new BusinessException(code, message, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException externalService(String code, String message) {
        return new BusinessException(code, message, HttpStatus.BAD_GATEWAY);
    }

    public static BusinessException internal(String code, String message) {
        return new BusinessException(code, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
