package com.civicconnect.citizen.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
 * Reads identity headers injected by the API Gateway
 * (X-User-Id, X-User-Email, X-User-Role) and populates the SecurityContext.
 *
 * If gateway headers are not present, falls back to parsing the JWT directly
 * from the Authorization header (useful for direct Swagger/Postman testing).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

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

        // ── Option 1: Gateway-injected headers ─────────────────────────────────
        if (StringUtils.hasText(userIdHeader) && StringUtils.hasText(roleHeader)) {
            try {
                Long userId   = Long.parseLong(userIdHeader);
                var authority = new SimpleGrantedAuthority("ROLE_" + roleHeader.toUpperCase());
                var auth      = new UsernamePasswordAuthenticationToken(
                        emailHeader, null, List.of(authority));
                auth.setDetails(userId);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (NumberFormatException ignored) {
                // malformed header — continue unauthenticated
            }
        }
        // ── Option 2: Direct JWT from Authorization header (for Swagger/Postman) ─
        else {
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
                    String role   = (String) claims.get("role");

                    var authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                    var auth      = new UsernamePasswordAuthenticationToken(
                            email, null, List.of(authority));
                    auth.setDetails(userId);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (JwtException | IllegalArgumentException ignored) {
                    // Invalid token — continue unauthenticated
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
