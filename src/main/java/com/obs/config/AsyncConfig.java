package com.obs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Enables asynchronous method execution (e.g., for sending emails without blocking requests)
}
