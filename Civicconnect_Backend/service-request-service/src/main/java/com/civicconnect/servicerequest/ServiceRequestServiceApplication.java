package com.civicconnect.servicerequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
/**
 * CivicConnect – Service Request Service (Port: 8083)
 *
 * Responsibilities:
 *  - Citizens submit service requests (ROAD / WATER / ELECTRICITY)
 *  - Department Head assigns officers to requests
 *  - Officers update request status (ASSIGNED → IN_PROGRESS → RESOLVED)
 *  - Citizens close or withdraw requests
 *  - Full update history tracked per request
 *
 * Feign clients:
 *  - citizen-service    → validate citizenId + check ACTIVE status before submit
 *  - identity-service   → validate officerId + check SERVICE_OFFICER role before assign
 *  - notification-service → send notifications on every status change
 *
 * DB: civicconnect_servicerequest
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ServiceRequestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRequestServiceApplication.class, args);
    }
}
