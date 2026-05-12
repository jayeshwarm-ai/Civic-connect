package com.civicconnect.reporting.config;

import com.civicconnect.reporting.security.JwtAuthenticationFilter;
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

    private static final String DEPARTMENT_HEAD    = "DEPARTMENT_HEAD";
    private static final String CITY_ADMINISTRATOR = "CITY_ADMINISTRATOR";
    private static final String COMPLIANCE_OFFICER = "COMPLIANCE_OFFICER";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/swagger-ui/**", "/swagger-ui.html",
                        "/api-docs/**", "/v3/api-docs/**",
                        "/actuator/**").permitAll()
                .requestMatchers("/internal/**").permitAll()

                // Generate: DEPARTMENT_HEAD | CITY_ADMINISTRATOR only
                .requestMatchers(HttpMethod.POST, "/api/v1/reports")
                    .hasAnyRole(DEPARTMENT_HEAD, CITY_ADMINISTRATOR)

                // Read: all three roles
                .requestMatchers(HttpMethod.GET, "/api/v1/reports/**")
                    .hasAnyRole(DEPARTMENT_HEAD, CITY_ADMINISTRATOR, COMPLIANCE_OFFICER)
                .requestMatchers(HttpMethod.GET, "/api/v1/reports")
                    .hasAnyRole(DEPARTMENT_HEAD, CITY_ADMINISTRATOR, COMPLIANCE_OFFICER)

                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
