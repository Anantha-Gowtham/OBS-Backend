package com.obs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// removed Order and AntPathRequestMatcher; using single filter chain
import org.springframework.web.cors.CorsConfigurationSource;
import com.obs.security.JwtAuthenticationFilter;
import com.obs.service.CustomUserDetailsService;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter, DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentTypeOptions -> {})
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/auth/**",
                    "/public/**",
                    "/health",
                    "/actuator/health",
                    "/h2-console/**",
                    "/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/test/**",
                    "/debug/public",
                    // Also allow context-path-prefixed forms when server.servlet.context-path=/api
                    "/api/auth/**",
                    "/api/public/**",
                    "/api/health",
                    "/api/actuator/health",
                    "/api/h2-console/**",
                    "/api/api-docs/**",
                    "/api/swagger-ui.html",
                    "/api/swagger-ui/**",
                    "/api/v3/api-docs/**",
                    "/api/test/**",
                    "/api/debug/public"
                ).permitAll()
                
                // Allow OPTIONS requests for CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Debug endpoints (authenticated)
                .requestMatchers("/debug/**", "/api/debug/**").authenticated()
                
                // Admin endpoints - Full system access
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                
                // Manager endpoints - Branch management
                .requestMatchers("/manager/**", "/api/manager/**").hasAnyRole("MANAGER", "ADMIN")
                
                // Employee endpoints - Customer service operations  
                .requestMatchers("/employee/**", "/api/employee/**").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")
                
                // User endpoints - Customer operations
                .requestMatchers("/user/**", "/api/user/**").hasAnyRole("USER", "EMPLOYEE", "MANAGER", "ADMIN")
                
                // All other requests need authentication
                .anyRequest().authenticated()
            );
        
        // Ensure our custom authentication provider is used
        http.authenticationProvider(daoAuthenticationProvider);
            
        // Add JWT filter before username/password authentication
        // But skip it entirely for auth endpoints using a conditional approach
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider daoAuthenticationProvider) {
        return new ProviderManager(daoAuthenticationProvider);
    }

    // Use only the @Service CustomUserDetailsService bean to avoid duplicates

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
