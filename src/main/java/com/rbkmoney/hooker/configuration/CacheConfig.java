package com.rbkmoney.hooker.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rbkmoney.hooker.model.InvoicingMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, InvoicingMessage> invoiceDataCache(@Value("${cache.invoice.size}") int cacheSize) {
        return Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .build();
    }
}
