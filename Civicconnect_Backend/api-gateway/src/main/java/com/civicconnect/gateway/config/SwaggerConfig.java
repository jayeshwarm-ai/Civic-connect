package com.civicconnect.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger aggregation configuration for API Gateway
 * Aggregates OpenAPI documentation from all downstream microservices
 */
@Configuration
public class SwaggerConfig {


    @Bean
    public List<org.springdoc.core.properties.SwaggerUiConfigProperties.SwaggerUrl> swaggerUrls() {
        List<org.springdoc.core.properties.SwaggerUiConfigProperties.SwaggerUrl> urls = new ArrayList<>();
        
        // Manually define the microservices for Swagger aggregation
        urls.add(new org.springdoc.core.properties.SwaggerUiConfigProperties.SwaggerUrl(
                "Identity Service", "/v3/api-docs/identity-service", null));
        urls.add(new org.springdoc.core.properties.SwaggerUiConfigProperties.SwaggerUrl(
                "Citizen Service", "/v3/api-docs/citizen-service", null));
        urls.add(new org.springdoc.core.properties.SwaggerUiConfigProperties.SwaggerUrl(
                "Service Request Service", "/v3/api-docs/service-request-service", null));
        urls.add(new org.springdoc.core.properties.SwaggerUiConfigProperties.SwaggerUrl(
                "Resolution Service", "/v3/api-docs/resolution-service", null));
        urls.add(new org.springdoc.core.properties.SwaggerUiConfigProperties.SwaggerUrl(
                "Feedback Service", "/v3/api-docs/feedback-service", null));
        urls.add(new org.springdoc.core.properties.SwaggerUiConfigProperties.SwaggerUrl(
                "Compliance Service", "/v3/api-docs/compliance-service", null));
        urls.add(new org.springdoc.core.properties.SwaggerUiConfigProperties.SwaggerUrl(
                "Reporting Service", "/v3/api-docs/reporting-service", null));
        urls.add(new org.springdoc.core.properties.SwaggerUiConfigProperties.SwaggerUrl(
                "Notification Service", "/v3/api-docs/notification-service", null));
        
        return urls;
    }
}

