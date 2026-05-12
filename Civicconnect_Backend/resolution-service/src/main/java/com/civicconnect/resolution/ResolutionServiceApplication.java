package com.civicconnect.resolution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * CivicConnect – Resolution Service (Port: 8084)
 *
 * Responsibilities:
 *  - Officers create resolutions for ASSIGNED service requests
 *  - Officers add and manage workflow steps
 *  - Step completion auto-triggers resolution completion
 *  - Resolution completion auto-pushes RESOLVED status to service-request-service
 *
 * Feign clients:
 *  - service-request-service → validate request status + push RESOLVED status back
 *  - identity-service        → validate officer role + validate step assignee role + audit logs
 *  - notification-service    → notify citizen on resolution created and resolved
 *
 * DB: civicconnect_resolution
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ResolutionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResolutionServiceApplication.class, args);
    }
}
