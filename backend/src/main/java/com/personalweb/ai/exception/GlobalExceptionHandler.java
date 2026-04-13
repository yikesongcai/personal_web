package com.personalweb.ai.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex, ServerWebExchange exchange) {
        return ResponseEntity.status(ex.getStatus())
                .body(buildResponse(ex.getCode(), ex.getMessage(), exchange));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiErrorResponse> handleBind(WebExchangeBindException ex, ServerWebExchange exchange) {
        String message = ex.getAllErrors().isEmpty()
                ? "请求参数校验失败"
                : ex.getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse("VALIDATION_ERROR", message, exchange));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAny(Exception ex, ServerWebExchange exchange) {
        log.error("Internal Server Error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildResponse("INTERNAL_ERROR", "服务器内部错误，请稍后再试", exchange));
    }

    private ApiErrorResponse buildResponse(String code, String message, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        return new ApiErrorResponse(code, message, path, LocalDateTime.now());
    }
}
