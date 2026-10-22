package com.assessment.card_product_management.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.exchange-rates")
public record ExchangeRateProperties(
        URI providerBaseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        Duration currencyCacheTtl,
        Duration rateCacheTtl
) {
}
