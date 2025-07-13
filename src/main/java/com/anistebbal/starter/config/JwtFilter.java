package com.anistebbal.starter.config;

import com.anistebbal.starter.services.JWTService;
import com.anistebbal.starter.services.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JWTService jwtService;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info(" JWT Filter triggered for path: {}", path);

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            log.warn(" No Authorization header provided. Skipping JWT validation.");
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            log.warn(" Authorization header malformed: '{}'", authHeader);
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String phone = null;

        try {
            phone = jwtService.extractPhone(token);
            log.info(" Extracted phone from token: {}", phone);
        } catch (Exception e) {
            log.error(" Failed to extract phone from JWT: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(phone);
            if (jwtService.validateToken(token, userDetails)) {
                log.info(" JWT is valid. Authenticating user: {}", phone);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                log.info("User roles: {}", userDetails.getAuthorities());
                log.info("Secured path: {}", request.getRequestURI());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                log.warn(" JWT validation failed for user: {}", phone);
            }
        }

        filterChain.doFilter(request, response);
    }
}
