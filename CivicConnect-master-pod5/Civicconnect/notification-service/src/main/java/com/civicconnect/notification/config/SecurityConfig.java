package com.civicconnect.notification.config;

import com.civicconnect.notification.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Swagger / Actuator / Internal ──────────────────────────
                .requestMatchers(
                        "/swagger-ui/**", "/swagger-ui.html",
                        "/api-docs/**", "/v3/api-docs/**",
                        "/actuator/**").permitAll()

                // ── Internal send endpoint — no JWT needed ─────────────────
                .requestMatchers("/internal/**").permitAll()

                // ── All authenticated users can access their notifications ─
                .requestMatchers("/api/v1/notifications/**")
                    .hasAnyRole("CITIZEN", "SERVICE_OFFICER", "DEPARTMENT_HEAD",
                            "CITY_ADMINISTRATOR", "COMPLIANCE_OFFICER")

                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
