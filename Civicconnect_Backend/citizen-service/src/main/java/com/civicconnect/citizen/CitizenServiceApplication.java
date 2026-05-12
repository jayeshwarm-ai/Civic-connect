package com.civicconnect.citizen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * CivicConnect – Citizen Service (Port: 8082)
 *
 * Responsibilities:
 *  - Citizen self-registration (creates User + Citizen profile)
 *  - Profile management (update address, phone, contactInfo)
 *  - Document upload and verification workflow
 *  - Account activation after document verification
 *
 * Feign clients:
 *  - identity-service → validate email/phone uniqueness before registration,
 *                        write audit logs, activate user account
 *
 * DB: civicconnect_citizen
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class CitizenServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitizenServiceApplication.class, args);
    }
}
