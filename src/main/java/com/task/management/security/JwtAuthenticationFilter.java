package com.task.management.security;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    
    private JwtHelper jwtHelper;
    private UserDetailsService userDetailsService;
    
    JwtAuthenticationFilter(JwtHelper jwtHelper, UserDetailsService userDetailsService) {
		this.jwtHelper = jwtHelper;
		this.userDetailsService = userDetailsService;
	}

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            String email = extractEmail(token);

            if (email != null && isNotAuthenticated()) {
                authenticateUser(email, token, request);
            }
        } else {
            log.info("Invalid Authorization header value or header missing");
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }

        return header.substring(7);
    }

    private String extractEmail(String token) {
        try {
            String email = jwtHelper.getUsernameFromToken(token);
            log.debug("Extracted email from token: {}", email);
            return email;

        } catch (IllegalArgumentException e) {
            log.warn("Illegal argument while fetching email from token", e);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT token is expired", e);
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("Invalid JWT token", e);
        } catch (Exception e) {
            log.error("Unexpected error while processing JWT token", e);
        }

        return null;
    }

    private boolean isNotAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() == null;
    }

    private void authenticateUser(String email, String token, HttpServletRequest request) {

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (jwtHelper.validateToken(token, userDetails)) {

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("JWT Token validated successfully, authentication set for user: {}", email);

        } else {
            log.info("JWT Token validation failed for user: {}", email);
        }
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/api/user/login")
            || path.startsWith("/api/admin/login")
            || path.startsWith("/api/user/register")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs")
            || path.equals("/swagger-ui.html")
            || path.equals("/favicon.ico");
    }

   
}
