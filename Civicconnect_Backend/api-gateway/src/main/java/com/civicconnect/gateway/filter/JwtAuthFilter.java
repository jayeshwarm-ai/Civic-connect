package com.civicconnect.gateway.filter;

import com.civicconnect.gateway.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Global JWT Authentication Filter.
 *
 * Runs on EVERY request before routing.
 * - Public paths are allowed through without a token.
 * - All other paths require a valid Bearer JWT.
 * - On success: injects X-User-Id, X-User-Email, X-User-Role headers
 *   so downstream services can trust the caller identity without
 *   re-validating the token themselves.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /** Paths that bypass JWT validation entirely */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/register-staff",
            "/api/v1/citizens/register",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/webjars/**",
            "/swagger-resources/**",
            "/actuator/**",
            "/*/actuator/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // ── 1. Let public paths through ────────────────────────────────────
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // ── 2. Extract Bearer token ─────────────────────────────────────────
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or malformed Authorization header for path: {}", path);
            return unauthorized(exchange, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(7);

        // ── 3. Validate token ───────────────────────────────────────────────
        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid or expired JWT for path: {}", path);
            return unauthorized(exchange, "Invalid or expired JWT token");
        }

        // ── 4. Extract claims and forward as headers ────────────────────────
        Long userId   = jwtUtil.extractUserId(token);
        String email  = jwtUtil.extractEmail(token);
        String role   = jwtUtil.extractRole(token);

        log.debug("Authenticated request — userId={}, role={}, path={}", userId, role, path);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id",    String.valueOf(userId))
                .header("X-User-Email", email)
                .header("X-User-Role",  role)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /** Highest priority — runs before all other filters */
    @Override
    public int getOrder() {
        return -100;
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}
