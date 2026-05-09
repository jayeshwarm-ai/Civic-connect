package com.civicconnect.citizen.config;

import com.civicconnect.citizen.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Public ─────────────────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/v1/citizens/register").permitAll()
                .requestMatchers(
                        "/swagger-ui/**", "/swagger-ui.html",
                        "/api-docs/**",   "/v3/api-docs/**",
                        "/actuator/**").permitAll()

                // ── Internal (Feign calls from other services) ──────────────
                .requestMatchers("/internal/**").permitAll()

                // ── Admin only ──────────────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/v1/citizens")
                    .hasRole(CITY_ADMINISTRATOR)
                .requestMatchers(HttpMethod.PATCH, "/api/v1/citizens/{citizenId}/deactivate")
                    .hasRole(CITY_ADMINISTRATOR)

                // ── Officer / Admin: document management ────────────────────
                .requestMatchers(HttpMethod.GET,   "/api/v1/citizens/documents/pending")
                    .hasAnyRole(SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMINISTRATOR)
                .requestMatchers(HttpMethod.PATCH, "/api/v1/citizens/documents/{documentId}/verify")
                    .hasAnyRole(SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMINISTRATOR)
                .requestMatchers(HttpMethod.GET, "/api/v1/citizens/documents/{documentId}/download")
                    .hasAnyRole(CITIZEN, SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMINISTRATOR)

                // ── Citizen only: write actions ─────────────────────────────
                // My documents upload (citizen self-service)
                .requestMatchers(HttpMethod.POST, "/api/v1/citizens/my-documents")
                    .hasRole(CITIZEN)
                // My profile update (citizen self-service)
                .requestMatchers(HttpMethod.PUT,  "/api/v1/citizens/my-profile")
                    .hasRole(CITIZEN)
                // Admin document upload for specific citizen
                .requestMatchers(HttpMethod.POST, "/api/v1/citizens/{citizenId}/documents")
                    .hasRole(CITIZEN)

                // ── Citizen only: reads ─────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/v1/citizens/my-profile")
                    .hasRole(CITIZEN)
                .requestMatchers(HttpMethod.GET, "/api/v1/citizens/my-documents")
                    .hasRole(CITIZEN)

                // ── Shared reads (all authenticated users) ───────────────────
                .requestMatchers(HttpMethod.GET, "/api/v1/citizens/by-user/{userId}")
                    .hasAnyRole(CITIZEN, SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMINISTRATOR)
                .requestMatchers(HttpMethod.GET, "/api/v1/citizens/{citizenId}")
                    .hasAnyRole(CITIZEN, SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMINISTRATOR)
                .requestMatchers(HttpMethod.GET, "/api/v1/citizens/{citizenId}/documents")
                    .hasAnyRole(CITIZEN, SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMINISTRATOR)

                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
