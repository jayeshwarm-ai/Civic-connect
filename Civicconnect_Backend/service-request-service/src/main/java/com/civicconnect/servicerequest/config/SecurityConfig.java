package com.civicconnect.servicerequest.config;

import com.civicconnect.servicerequest.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String CITIZEN            = "CITIZEN";
    private static final String SERVICE_OFFICER    = "SERVICE_OFFICER";
    private static final String DEPARTMENT_HEAD    = "DEPARTMENT_HEAD";
    private static final String CITY_ADMINISTRATOR = "CITY_ADMINISTRATOR";
    private static final String COMPLIANCE_OFFICER = "COMPLIANCE_OFFICER";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Swagger / Actuator / Internal ──────────────────────────
                .requestMatchers(
                        "/swagger-ui/**", "/swagger-ui.html",
                        "/api-docs/**",   "/v3/api-docs/**",
                        "/actuator/**").permitAll()
                .requestMatchers("/internal/**").permitAll()

                // ── DEPARTMENT_HEAD | CITY_ADMINISTRATOR: assign officer ───
                .requestMatchers(HttpMethod.PATCH, "/api/v1/service-requests/*/assign")
                    .hasAnyRole(DEPARTMENT_HEAD, CITY_ADMINISTRATOR)

                // ── SERVICE_OFFICER+: officer dashboard & status update ────
                .requestMatchers(HttpMethod.PATCH, "/api/v1/service-requests/*/status")
                    .hasAnyRole(SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMINISTRATOR)
                .requestMatchers(HttpMethod.GET, "/api/v1/service-requests/officer/**")
                    .hasAnyRole(SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMINISTRATOR)
                .requestMatchers(HttpMethod.GET, "/api/v1/service-requests")
                    .hasAnyRole(SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMINISTRATOR, COMPLIANCE_OFFICER)

                // ── CITIZEN only: write actions ────────────────────────────
                .requestMatchers(HttpMethod.POST,   "/api/v1/service-requests")
                    .hasRole(CITIZEN)
                .requestMatchers(HttpMethod.PUT,    "/api/v1/service-requests/*")
                    .hasRole(CITIZEN)
                .requestMatchers(HttpMethod.PATCH,  "/api/v1/service-requests/*/close")
                    .hasRole(CITIZEN)
                .requestMatchers(HttpMethod.DELETE, "/api/v1/service-requests/**")
                    .hasRole(CITIZEN)

                // ── Shared reads ───────────────────────────────────────────
                // COMPLIANCE_OFFICER needs GET access to verify a request is
                // CLOSED before creating a compliance record on it.
                .requestMatchers(HttpMethod.GET, "/api/v1/service-requests/**")
                    .hasAnyRole(CITIZEN, SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMINISTRATOR, COMPLIANCE_OFFICER)

                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
