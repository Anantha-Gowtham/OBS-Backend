package com.obs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// @Configuration - Commented out to avoid duplicate bean with SecurityConfig
public class PasswordEncoderConfig {
    
    // @Bean - Moved to SecurityConfig to avoid duplication
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
