package com.civicconnect.resolution.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads X-User-Id, X-User-Email, X-User-Role headers injected by the API Gateway
 * and populates Spring Security context. Same pattern as all other services.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

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
                // malformed header — continue unauthenticated
            }
        }

        filterChain.doFilter(request, response);
    }
}
