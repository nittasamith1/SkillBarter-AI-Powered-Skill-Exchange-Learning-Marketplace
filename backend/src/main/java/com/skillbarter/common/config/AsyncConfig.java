package com.skillbarter.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables Spring's @Async support for background processing (e.g., audit logging).
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
