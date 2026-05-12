package com.civicconnect.compliance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * CivicConnect – Compliance Service (Port: 8086)
 *
 * Responsibilities:
 *  - COMPLIANCE_OFFICERs create compliance records (PASS/FAIL) for requests and resolutions
 *  - COMPLIANCE_OFFICERs manage audit records (OPEN → IN_REVIEW → CLOSED)
 *  - Audit records owned locally; compliance records reference cross-service entities by ID
 *
 * Feign clients:
 *  - service-request-service → validate ServiceRequest exists (REQUEST type compliance check)
 *  - resolution-service      → validate Resolution exists (RESOLUTION type compliance check)
 *  - identity-service        → validate officer role + write audit logs
 *  - notification-service    → notify on FAIL compliance records
 *
 * DB: civicconnect_compliance
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ComplianceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComplianceServiceApplication.class, args);
    }
}
