package com.personalweb.ai.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
public class AdminAuthFilter implements WebFilter {

    @Value("${ADMIN_API_KEY}")
    private String adminApiKey;

    private static final List<String> PROTECTED_PREFIXES = List.of(
            "/api/admin",
            "/api/knowledge",
            "/api/debug"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        boolean isProtected = PROTECTED_PREFIXES.stream().anyMatch(path::startsWith);
        
        if (!isProtected) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("X-Admin-Token");
        
        if (adminApiKey != null && adminApiKey.equals(token)) {
            return chain.filter(exchange);
        }

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String errorMsg = "{\"code\":\"UNAUTHORIZED\", \"message\":\"Admin Authentication Failed\"}";
        DataBuffer buffer = response.bufferFactory().wrap(errorMsg.getBytes());
        
        return response.writeWith(Mono.just(buffer));
    }
}
