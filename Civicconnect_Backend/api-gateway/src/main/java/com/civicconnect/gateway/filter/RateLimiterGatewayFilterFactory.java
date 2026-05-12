package com.civicconnect.gateway.filter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Custom Gateway Filter for Rate Limiting using Resilience4j.
 * 
 * Usage in application.yml:
 * filters:
 *   - name: RateLimiter
 *     args:
 *       rateLimiterName: authRateLimiter
 */
@Slf4j
@Component
public class RateLimiterGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<RateLimiterGatewayFilterFactory.Config> {

    private final RateLimiterRegistry rateLimiterRegistry;

    public RateLimiterGatewayFilterFactory(RateLimiterRegistry rateLimiterRegistry) {
        super(Config.class);
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try {
                // Get client identifier (IP address)
                String clientId = getClientId(exchange);
                
                // Create a unique rate limiter key: rateLimiterName + clientId
                String rateLimiterKey = config.getRateLimiterName() + "-" + clientId;
                
                // Get or create rate limiter for this client
                RateLimiter rateLimiter;
                try {
                    rateLimiter = rateLimiterRegistry.rateLimiter(
                            rateLimiterKey, 
                            config.getRateLimiterName()
                    );
                } catch (Exception e) {
                    // If rate limiter config not found, use default and log warning
                    log.warn("Rate limiter config '{}' not found, using 'defaultRateLimiter'. Error: {}", 
                            config.getRateLimiterName(), e.getMessage());
                    rateLimiter = rateLimiterRegistry.rateLimiter(
                            rateLimiterKey, 
                            "defaultRateLimiter"
                    );
                }

                // Try to acquire permission
                boolean permitted = rateLimiter.acquirePermission();

                if (permitted) {
                    log.debug("Rate limit permitted for client: {} on limiter: {}", 
                            clientId, config.getRateLimiterName());
                    return chain.filter(exchange);
                } else {
                    log.warn("Rate limit exceeded for client: {} on limiter: {}", 
                            clientId, config.getRateLimiterName());
                    
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    exchange.getResponse().getHeaders().add("X-RateLimit-Retry-After", 
                            String.valueOf(rateLimiter.getRateLimiterConfig().getLimitRefreshPeriod().getSeconds()));
                    
                    return exchange.getResponse().setComplete();
                }
            } catch (Exception e) {
                // If any error occurs in rate limiting, log and allow the request through
                log.error("Error in rate limiter filter: {}. Allowing request through.", e.getMessage(), e);
                return chain.filter(exchange);
            }
        };
    }

    /**
     * Extract client identifier from request.
     * Uses IP address as the key for rate limiting.
     */
    private String getClientId(org.springframework.web.server.ServerWebExchange exchange) {
        // Try to get real IP from X-Forwarded-For header (if behind proxy/load balancer)
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // Take the first IP if there are multiple
            return forwardedFor.split(",")[0].trim();
        }
        
        // Fallback to remote address
        if (exchange.getRequest().getRemoteAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        
        return "unknown";
    }

    @Override
    public String name() {
        return "RateLimiter";
    }

    /**
     * Configuration class for the filter.
     */
    public static class Config {
        private String rateLimiterName = "default";

        public String getRateLimiterName() {
            return rateLimiterName;
        }

        public void setRateLimiterName(String rateLimiterName) {
            this.rateLimiterName = rateLimiterName;
        }
    }
}


