package com.task.management.security;
import java.io.IOException;
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
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    
    private JwtHelper jwtHelper;
    private UserDetailsService userDetailsService;
    
    JwtAuthenticationFilter(JwtHelper jwtHelper, UserDetailsService userDetailsService) {
		this.jwtHelper = jwtHelper;
		this.userDetailsService = userDetailsService;
	}

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestHeader = request.getHeader("Authorization");

        String email = null;
        String token = null;

        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            token = requestHeader.substring(7);
            try {
                email = this.jwtHelper.getUsernameFromToken(token); // Extract email from token
                logger.debug("Extracted email from token: {}", email);
            } catch (IllegalArgumentException e) {
                logger.warn("Illegal argument while fetching email from token", e);
            } catch (ExpiredJwtException e) {
                logger.warn("JWT token is expired", e);
            } catch (MalformedJwtException e) {
                logger.warn("Invalid JWT token", e);
            } catch (Exception e) {
                logger.error("Unexpected error while processing JWT token", e);
            }
        } else {
            logger.info("Invalid Authorization header value or header missing");
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
            Boolean validateToken = this.jwtHelper.validateToken(token, userDetails);
            if (validateToken) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("JWT Token validated successfully, authentication set for user: {}", email);
            } else {
                logger.info("JWT Token validation failed for user: {}", email);
            }
        }

        filterChain.doFilter(request, response);
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
