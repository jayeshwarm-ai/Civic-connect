package com.civicconnect.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * CivicConnect – Centralised Configuration Server
 *
 * Serves per-service YAML configs from classpath:/config/
 * Pattern: /config/{service-name}/{profile}
 *
 * Example: GET http://localhost:8888/identity-service/default
 *
 * Startup order:
 *   1. eureka-server  (8761)
 *   2. config-server  (8888)  ← this service
 *   3. all other microservices
 */
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
