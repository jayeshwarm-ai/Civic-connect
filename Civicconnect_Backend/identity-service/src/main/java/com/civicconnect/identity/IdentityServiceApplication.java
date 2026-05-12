package com.civicconnect.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * CivicConnect – Identity & Access Management Service (Port: 8081)
 *
 * Responsibilities:
 *  - User authentication (login → JWT)
 *  - Staff account management (CITY_ADMINISTRATOR only)
 *  - IAM: profile lookup, user deactivation
 *  - Audit log storage and querying
 *  - AuditRecord management (Compliance Officer)
 *  - Internal API: expose UserValidation endpoint consumed by other services via Feign
 *
 * DB: civicconnect_identity
 */
@SpringBootApplication
@EnableDiscoveryClient
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
