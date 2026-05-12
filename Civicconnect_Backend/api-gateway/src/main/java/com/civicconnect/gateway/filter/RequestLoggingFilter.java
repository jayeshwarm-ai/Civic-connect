package com.civicconnect.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Logs every request and response passing through the gateway.
 * Runs after JwtAuthFilter (order -99).
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        long startTime = Instant.now().toEpochMilli();

        String userId = request.getHeaders().getFirst("X-User-Id");
        String role   = request.getHeaders().getFirst("X-User-Role");

        log.info("[GATEWAY] → {} {} | userId={} role={}",
                request.getMethod(),
                request.getURI().getPath(),
                userId != null ? userId : "anonymous",
                role   != null ? role   : "none");

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            long duration = Instant.now().toEpochMilli() - startTime;
            log.info("[GATEWAY] ← {} {} | status={} | {}ms",
                    request.getMethod(),
                    request.getURI().getPath(),
                    response.getStatusCode(),
                    duration);
        }));
    }

    @Override
    public int getOrder() {
        return -99;
    }
}
