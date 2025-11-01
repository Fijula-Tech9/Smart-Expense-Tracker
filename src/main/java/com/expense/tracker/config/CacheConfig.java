package com.expense.tracker.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Cache Configuration
 * 
 * Configures caching for improved performance of frequently
 * accessed data like reports and user information.
 * Uses in-memory cache for development.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * In-memory Cache Manager for development
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("monthlyReports", "categoryReports", "budgetAlerts", "userProfiles");
    }
}
