package com.civicconnect.identity.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads the Bearer token injected by the API Gateway
 * (headers: X-User-Id, X-User-Email, X-User-Role)
 * and populates the Spring Security context.
 *
 * The gateway has already validated the token — this service
 * just trusts the forwarded headers and reconstructs the auth object.
 * Falls back to direct JWT validation for local/direct calls.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ── 1. Try gateway-forwarded headers first ─────────────────────────
        String userIdHeader = request.getHeader("X-User-Id");
        String roleHeader   = request.getHeader("X-User-Role");
        String emailHeader  = request.getHeader("X-User-Email");

        if (StringUtils.hasText(userIdHeader) && StringUtils.hasText(roleHeader)) {
            try {
                Long userId   = Long.parseLong(userIdHeader);
                var authority = new SimpleGrantedAuthority("ROLE_" + roleHeader);
                var auth      = new UsernamePasswordAuthenticationToken(
                        emailHeader, null, List.of(authority));
                auth.setDetails(userId);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (NumberFormatException ignored) {
                // malformed header — fall through to JWT check
            }
            filterChain.doFilter(request, response);
            return;
        }

        // ── 2. Fallback: direct Bearer JWT (local calls / Swagger) ─────────
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validateToken(token)) {
                Long   userId = jwtUtil.extractUserId(token);
                String email  = jwtUtil.extractEmail(token);
                String role   = jwtUtil.extractRole(token);

                var authority = new SimpleGrantedAuthority("ROLE_" + role);
                var auth      = new UsernamePasswordAuthenticationToken(
                        email, null, List.of(authority));
                auth.setDetails(userId);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
