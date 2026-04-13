package com.personalweb.ai.config;

import java.time.Instant;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.personalweb.ai.service.RateLimitWhitelistService;

import reactor.core.publisher.Mono;

@Component
public class RateLimitFilter implements WebFilter {

    private final RateLimitWhitelistService whitelistService;
    
    // IP -> Deque of request timestamps
    private final ConcurrentHashMap<String, Deque<Long>> requestCounts = new ConcurrentHashMap<>();
    
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_IN_MS = 60000;

    public RateLimitFilter(RateLimitWhitelistService whitelistService) {
        this.whitelistService = whitelistService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        
        if (!"/api/chat".equals(path)) {
            return chain.filter(exchange);
        }

        String ip = null;
        if (exchange.getRequest().getRemoteAddress() != null) {
            ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            ip = forwardedFor.split(",")[0].trim();
        }

        if (ip == null || whitelistService.isWhitelisted(ip)) {
            return chain.filter(exchange);
        }

        long now = Instant.now().toEpochMilli();
        Deque<Long> timestamps = requestCounts.computeIfAbsent(ip, k -> new LinkedList<>());
        
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_IN_MS) {
                timestamps.pollFirst();
            }
            
            if (timestamps.size() >= MAX_REQUESTS) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                
                String errorMsg = "{\"code\":\"TOO_MANY_REQUESTS\", \"message\":\"Chat rate limit exceeded. Max 10 requests per minute.\"}";
                DataBuffer buffer = response.bufferFactory().wrap(errorMsg.getBytes());
                
                return response.writeWith(Mono.just(buffer));
            }
            
            timestamps.addLast(now);
        }
        
        return chain.filter(exchange);
    }
}
