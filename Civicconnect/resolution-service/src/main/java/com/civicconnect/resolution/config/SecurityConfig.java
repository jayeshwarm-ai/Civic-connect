package com.civicconnect.resolution.config;

import com.civicconnect.resolution.security.JwtAuthenticationFilter;
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

                // ── Public (Swagger / Actuator / Internal) ─────────────────
                .requestMatchers(
                        "/swagger-ui/**", "/swagger-ui.html",
                        "/api-docs/**",   "/v3/api-docs/**",
                        "/actuator/**").permitAll()
                .requestMatchers("/internal/**").permitAll()

                // ── COMPLIANCE_OFFICER: read-only access to resolutions ────
                // Needed to verify a resolution is COMPLETED before recording
                // a compliance check on it.
                .requestMatchers(HttpMethod.GET, "/api/v1/resolutions/**")
                    .hasAnyRole(SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMINISTRATOR, COMPLIANCE_OFFICER)

                // ── Write actions: SERVICE_OFFICER and above only ──────────
                // Citizens never interact with resolutions directly.
                .requestMatchers("/api/v1/resolutions/**")
                    .hasAnyRole(SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMINISTRATOR)

                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
