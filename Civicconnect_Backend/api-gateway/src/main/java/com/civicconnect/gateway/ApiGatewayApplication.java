package com.civicconnect.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * CivicConnect – API Gateway
 *
 * Single entry point for ALL client requests.
 * Responsibilities:
 *  - JWT authentication validation (JwtAuthFilter)
 *  - Route forwarding to downstream microservices via Eureka load-balancer
 *  - Circuit breaker per route
 *  - Request/Response logging
 *
 * All routes use lb:// prefix → load-balanced via Eureka discovery.
 *
 * Startup order:
 *   1. eureka-server  (8761)
 *   2. config-server  (8888)
 *   3. api-gateway    (8080)  ← this service
 *   4. all other microservices
 */
@SpringBootApplication
@EnableDiscoveryClient
public class  ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
