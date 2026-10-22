package com.assessment.card_product_management.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CARD_PRODUCT_PAGES_CACHE = "card-product-pages";
    public static final String EXCHANGE_RATE_CURRENCIES_CACHE = "exchange-rate-currencies";
    public static final String EXCHANGE_RATES_CACHE = "exchange-rates";

    @Bean
    CacheManager cacheManager(ExchangeRateProperties exchangeRateProperties) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                new CaffeineCache(CARD_PRODUCT_PAGES_CACHE, Caffeine.newBuilder()
                        .maximumSize(100)
                        .expireAfterWrite(Duration.ofSeconds(60))
                        .build()),
                new CaffeineCache(EXCHANGE_RATE_CURRENCIES_CACHE, Caffeine.newBuilder()
                        .maximumSize(1)
                        .expireAfterWrite(exchangeRateProperties.currencyCacheTtl())
                        .build()),
                new CaffeineCache(EXCHANGE_RATES_CACHE, Caffeine.newBuilder()
                        .maximumSize(200)
                        .expireAfterWrite(exchangeRateProperties.rateCacheTtl())
                        .build())
        ));
        cacheManager.initializeCaches();
        return new TransactionAwareCacheManagerProxy(cacheManager);
    }
}
