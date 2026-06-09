package com.insurance.sample.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Value("${app.cache.policy-list-ttl-seconds:60}")
    private long policyListTtl;

    @Value("${app.cache.policy-detail-ttl-seconds:300}")
    private long policyDetailTtl;

    @Value("${app.cache.policy-summary-ttl-seconds:120}")
    private long policySummaryTtl;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache("policy-list",
                Caffeine.newBuilder()
                        .expireAfterWrite(policyListTtl, TimeUnit.SECONDS)
                        .maximumSize(200)
                        .recordStats()
                        .build());
        manager.registerCustomCache("policy-detail",
                Caffeine.newBuilder()
                        .expireAfterWrite(policyDetailTtl, TimeUnit.SECONDS)
                        .maximumSize(500)
                        .recordStats()
                        .build());
        manager.registerCustomCache("policy-summary",
                Caffeine.newBuilder()
                        .expireAfterWrite(policySummaryTtl, TimeUnit.SECONDS)
                        .maximumSize(1)
                        .recordStats()
                        .build());
        return manager;
    }
}
