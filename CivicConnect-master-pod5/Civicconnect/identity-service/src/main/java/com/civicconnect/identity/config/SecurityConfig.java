package com.civicconnect.identity.config;

import com.civicconnect.identity.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
    private static final String COMPLIANCE_OFFICER = "COMPLIANCE_OFFICER";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Public ────────────────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/reset-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register-staff").permitAll()
                .requestMatchers(
                        "/swagger-ui/**", "/swagger-ui.html",
                        "/api-docs/**",   "/v3/api-docs/**",
                        "/actuator/**").permitAll()

                // ── Internal API (called by other microservices via Feign) ─
                // /internal/users/**       → citizen/service-request/resolution/feedback
                // /internal/audit-logs     → all services write audit trail here
                .requestMatchers("/internal/**").permitAll()

                // ── Self-service: any signed-in staff role can view/edit own profile ─
                // (Citizens use /api/v1/citizens/my-profile instead — different service)
                .requestMatchers(HttpMethod.GET,   "/api/v1/users/me")
                    .hasAnyRole("SERVICE_OFFICER", "DEPARTMENT_HEAD", "CITY_ADMINISTRATOR", "COMPLIANCE_OFFICER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/users/me")
                    .hasAnyRole("SERVICE_OFFICER", "DEPARTMENT_HEAD", "COMPLIANCE_OFFICER")

                // ── Staff Management — CITY_ADMINISTRATOR only ─────────────
                .requestMatchers("/api/v1/users/**")
                    .hasRole(CITY_ADMINISTRATOR)

                // ── IAM: deactivate — CITY_ADMINISTRATOR only ─────────────
                .requestMatchers(HttpMethod.PATCH, "/api/v1/iam/users/*/deactivate")
                    .hasRole(CITY_ADMINISTRATOR)

                // ── Audit logs — CITY_ADMINISTRATOR only ──────────────────
                .requestMatchers("/api/v1/iam/audit-logs")
                    .hasRole(CITY_ADMINISTRATOR)
                .requestMatchers("/api/v1/iam/audit-logs/resource")
                    .hasRole(CITY_ADMINISTRATOR)
                .requestMatchers("/api/v1/iam/audit-logs/action")
                    .hasRole(CITY_ADMINISTRATOR)

                // ── Audit logs by user — ADMIN + COMPLIANCE_OFFICER ───────
                .requestMatchers("/api/v1/iam/audit-logs/user/**")
                    .hasAnyRole(CITY_ADMINISTRATOR, COMPLIANCE_OFFICER)

                // ── AuditRecord (formal compliance audits) ────────────────
                .requestMatchers("/api/v1/audit-records/**")
                    .hasAnyRole(COMPLIANCE_OFFICER, CITY_ADMINISTRATOR)

                // ── Profile (any authenticated role) ──────────────────────
                .requestMatchers(HttpMethod.GET, "/api/v1/iam/me")
                    .hasAnyRole(CITIZEN, SERVICE_OFFICER, DEPARTMENT_HEAD,
                                CITY_ADMINISTRATOR, COMPLIANCE_OFFICER)

                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
