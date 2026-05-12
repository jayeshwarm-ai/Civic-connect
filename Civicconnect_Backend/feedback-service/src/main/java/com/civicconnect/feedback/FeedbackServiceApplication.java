package com.civicconnect.feedback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * CivicConnect – Feedback Service (Port: 8085)
 *
 * Responsibilities:
 *  - Citizens submit feedback for CLOSED service requests (rating 1-5 + comments)
 *  - Satisfaction metrics tracked per officer (running average)
 *  - Leaderboard of officer satisfaction scores
 *
 * Feign clients:
 *  - service-request-service → verify request is CLOSED + get assignedOfficerUserId
 *  - citizen-service          → verify citizenId + check ownership
 *  - identity-service         → validate officerId + write audit logs
 *  - notification-service     → notify assigned officer on feedback submitted
 *
 * DB: civicconnect_feedback
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class FeedbackServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeedbackServiceApplication.class, args);
    }
}
