package com.civicconnect.gateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback responses returned when a downstream service's circuit breaker
 * trips open (service is down or too slow).
 *
 * Each route in application.yml references a specific fallback URI:
 *   /fallback/identity, /fallback/citizen, etc.
 */

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/identity")
    public Mono<ResponseEntity<Map<String, Object>>> identityFallback() {
        return fallback("identity-service", "Authentication/User service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/citizen")
    public Mono<ResponseEntity<Map<String, Object>>> citizenFallback() {
        return fallback("citizen-service", "Citizen management service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/service-request")
    public Mono<ResponseEntity<Map<String, Object>>> serviceRequestFallback() {
        return fallback("service-request-service", "Service request system is currently unavailable. Please try again later.");
    }

    @RequestMapping("/resolution")
    public Mono<ResponseEntity<Map<String, Object>>> resolutionFallback() {
        return fallback("resolution-service", "Resolution management service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/feedback")
    public Mono<ResponseEntity<Map<String, Object>>> feedbackFallback() {
        return fallback("feedback-service", "Feedback service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/compliance")
    public Mono<ResponseEntity<Map<String, Object>>> complianceFallback() {
        return fallback("compliance-service", "Compliance service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/reporting")
    public Mono<ResponseEntity<Map<String, Object>>> reportingFallback() {
        return fallback("reporting-service", "Reporting service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/notification")
    public Mono<ResponseEntity<Map<String, Object>>> notificationFallback() {
        return fallback("notification-service", "Notification service is currently unavailable. Please try again later.");
    }

    // ── Helper ─────────────────────────────────────────────────────────────

    private Mono<ResponseEntity<Map<String, Object>>> fallback(String service, String message) {
        Map<String, Object> body = Map.of(
                "status",    503,
                "error",     "Service Unavailable",
                "service",   service,
                "message",   message,
                "timestamp", LocalDateTime.now().toString()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body));
    }
}
