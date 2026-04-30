package com.civicconnect.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.beans.factory.annotation.Value;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * WebFlux configuration for Swagger UI redirection
 * Ensures Swagger UI is properly accessible in Spring Cloud Gateway (port 9999)
 */
@Configuration
public class SwaggerUiWebFluxConfigurer {

    @Bean
    public RouterFunction<ServerResponse> swaggerRouterFunction() {
        return route(GET("/swagger-ui/"), 
                req -> ServerResponse.permanentRedirect(
                        java.net.URI.create("/swagger-ui.html")).build())
            .andRoute(GET("/swagger-ui"), 
                req -> ServerResponse.permanentRedirect(
                        java.net.URI.create("/swagger-ui.html")).build())
            .andRoute(GET("/"), 
                req -> ServerResponse.permanentRedirect(
                        java.net.URI.create("/swagger-ui.html")).build());
    }
}

