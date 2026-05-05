package com.task.management.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.task.management.security.JwtAuthenticationEntryPoint;
import com.task.management.security.JwtAuthenticationFilter;
import org.springframework.http.HttpMethod;



@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    
    private JwtAuthenticationEntryPoint point;
    private JwtAuthenticationFilter filter;
    
    SecurityConfig(JwtAuthenticationEntryPoint point,
            JwtAuthenticationFilter filter
         ) {
				 this.point = point;
				 this.filter = filter;
				 logger.info("SecurityConfig initialized");
			}

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http){
    	try {
	        logger.info("Initializing security filter chain...");
	
	        http
	            .csrf(csrf -> csrf.disable())
	            .cors(cors -> logger.info("CORS support enabled in Spring Security"))
	            .authorizeHttpRequests(auth -> {
	                logger.info("Setting security rules for HTTP requests");
	                auth
	                	
	                    .requestMatchers("/api/tenant/register" ,
	                    		"/api/auth/login").permitAll()
	                    .requestMatchers(HttpMethod.GET,"/api/user").hasAnyRole("USER","MANAGER","ADMIN")
	                    .requestMatchers(HttpMethod.POST,"/api/user").hasRole("ADMIN")
	                    .requestMatchers("/api/task/*/assign").hasRole("MANAGER")
	                    .requestMatchers("/api/task/*/status").hasRole("USER")
	                    
	                    .anyRequest().authenticated();
	            })
	            .exceptionHandling(ex -> {
	                logger.info("Registering authentication entry point for exception handling");
	                ex.authenticationEntryPoint(point);
	            })
	            .sessionManagement(session -> {
	                logger.info("Configuring stateless session management");
	                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	            });
	
	        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
	        logger.info("JWT authentication filter registered");
	
	        return http.build();
    	}catch(Exception e) {
    		logger.error("uable to authenticate user {}",e.getMessage());
    		return null;
    	}
		
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        logger.info("Defining CORS configuration source...");

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:8080"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        logger.info("CORS allowed origins: {}", config.getAllowedOrigins());
        logger.info("CORS allowed methods: {}", config.getAllowedMethods());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        logger.info("CORS configuration applied to all endpoints");

        return source;
    }
}
