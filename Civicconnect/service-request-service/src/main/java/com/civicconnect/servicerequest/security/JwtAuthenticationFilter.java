package com.civicconnect.servicerequest.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.Base64;
import java.util.List;

/**
 * Reads X-User-Id, X-User-Email, X-User-Role headers injected by the API Gateway
 * and populates the Spring Security context.
 *
 * If gateway headers are not present, falls back to parsing the JWT directly
 * from the Authorization header (useful for direct Swagger/Postman testing).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final Key signingKey;

    public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userIdHeader = request.getHeader("X-User-Id");
        String roleHeader   = request.getHeader("X-User-Role");
        String emailHeader  = request.getHeader("X-User-Email");

        boolean authenticated = false;

        // ── Option 1: Gateway-injected headers ─────────────────────────────────
        if (StringUtils.hasText(userIdHeader) && StringUtils.hasText(roleHeader)) {
            try {
                Long userId   = Long.parseLong(userIdHeader);
                // Trim whitespace and uppercase for consistent role matching.
                String role = roleHeader.trim().toUpperCase();
                var authority = new SimpleGrantedAuthority("ROLE_" + role);
                var auth      = new UsernamePasswordAuthenticationToken(
                        emailHeader, null, List.of(authority));
                auth.setDetails(userId);
                SecurityContextHolder.getContext().setAuthentication(auth);
                authenticated = true;
                log.debug("Authenticated via gateway headers — userId={}, role={}, path={}",
                          userId, role, request.getRequestURI());
            } catch (NumberFormatException ignored) {
                log.warn("Malformed X-User-Id header: '{}'", userIdHeader);
            }
        }

        // ── Option 2: Direct JWT from Authorization header (Swagger/Postman, OR
        //              fallback when gateway forgot to inject headers).
        //              We also enter this branch if Option 1 failed to set auth,
        //              so a partial/missing header set still gets a chance to
        //              succeed via the raw token.
        if (!authenticated) {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(signingKey)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

                    Long userId   = ((Number) claims.get("userId")).longValue();
                    String email  = claims.getSubject();
                    String roleRaw = (String) claims.get("role");
                    String role   = roleRaw == null ? "" : roleRaw.trim().toUpperCase();

                    if (!role.isEmpty()) {
                        var authority = new SimpleGrantedAuthority("ROLE_" + role);
                        var auth      = new UsernamePasswordAuthenticationToken(
                                email, null, List.of(authority));
                        auth.setDetails(userId);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("Authenticated via raw JWT — userId={}, role={}, path={}",
                                  userId, role, request.getRequestURI());
                    }
                } catch (JwtException | IllegalArgumentException ex) {
                    log.warn("Invalid JWT for path {}: {}", request.getRequestURI(), ex.getMessage());
                }
            } else {
                log.debug("No authentication on request to {} — gateway headers missing and no Bearer token",
                          request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }
}
