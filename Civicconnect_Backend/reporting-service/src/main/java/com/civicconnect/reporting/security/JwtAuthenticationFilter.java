package com.civicconnect.reporting.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String userIdHeader = req.getHeader("X-User-Id");
        String roleHeader   = req.getHeader("X-User-Role");
        String emailHeader  = req.getHeader("X-User-Email");
        if (StringUtils.hasText(userIdHeader) && StringUtils.hasText(roleHeader)) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                var auth = new UsernamePasswordAuthenticationToken(
                        emailHeader, null, List.of(new SimpleGrantedAuthority("ROLE_" + roleHeader)));
                auth.setDetails(userId);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (NumberFormatException ignored) {}
        }
        chain.doFilter(req, res);
    }
}
